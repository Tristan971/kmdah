package moe.tristan.kmdah.operator.service.workers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.common.api.worker.Worker;
import moe.tristan.kmdah.common.api.worker.WorkerConfiguration;
import moe.tristan.kmdah.common.api.worker.WorkerShutdown;
import moe.tristan.kmdah.common.model.settings.MangadexSettings;
import moe.tristan.kmdah.operator.service.mangadex.PingResponseReceivedEvent;


@Service
public class WorkerPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerPool.class);

    private final MangadexSettings mangadexSettings;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final AtomicReference<String> imageServer = new AtomicReference<>();
    private final Map<Worker, Instant> workersAndExpiries = new ConcurrentHashMap<>();

    public WorkerPool(MangadexSettings mangadexSettings, ApplicationEventPublisher applicationEventPublisher) {
        this.mangadexSettings = mangadexSettings;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public WorkerConfiguration heartbeat(Worker worker) {
        Instant previous = workersAndExpiries.put(worker, Instant.now().plus(30, ChronoUnit.SECONDS));

        if (previous == null) {
            LOGGER.info("Successfully registered worker: {}", worker.getUniqueName());
            logPoolStatus();
            applicationEventPublisher.publishEvent(WorkerRegisteredEvent.builder().build());
        } else {
            LOGGER.debug("Heartbeat received from worker: {}", worker);
        }

        return WorkerConfiguration.of(Optional.ofNullable(imageServer.get()));
    }

    public WorkerShutdown disconnect(Worker worker) {
        workersAndExpiries.remove(worker);
        LOGGER.info("Successfully unregistered worker: {}", worker);
        logPoolStatus();

        if (workersAndExpiries.isEmpty()) {
            applicationEventPublisher.publishEvent(WorkerPoolEmptiedEvent.builder().build());
        }

        OptionalInt workerGracefulShutdownSeconds =
            workersAndExpiries.isEmpty()
            ? OptionalInt.of(mangadexSettings.getGracefulShutdownSeconds())
            : OptionalInt.empty();

        return WorkerShutdown
            .builder()
            .gracefulShutdownSeconds(workerGracefulShutdownSeconds)
            .build();
    }

    @Scheduled(fixedDelay = 5_000)
    protected void reapExpired() {
        Instant now = Instant.now();

        Set<Worker> expiredWorkers = workersAndExpiries
            .keySet()
            .stream()
            .filter(worker -> workersAndExpiries.get(worker).isBefore(now))
            .collect(Collectors.toSet());

        if (!expiredWorkers.isEmpty()) {
            LOGGER.error("Unregistering expired workers: {}", expiredWorkers);
            expiredWorkers.forEach(workersAndExpiries::remove);
            logPoolStatus();

            if (workersAndExpiries.isEmpty()) {
                applicationEventPublisher.publishEvent(WorkerPoolEmptiedEvent.builder().build());
            }
        }
    }

    public long getPoolBandwidthMegabitsPerSecond() {
        return workersAndExpiries
            .keySet()
            .stream()
            .mapToLong(Worker::getBandwidthMegabitsPerSecond)
            .reduce(Long::sum)
            .orElse(-1L);
    }

    @EventListener(PingResponseReceivedEvent.class)
    public void pingReceived(PingResponseReceivedEvent event) {
        this.imageServer.set(event.getPingResponse().getImageServer());
    }

    private void logPoolStatus() {
        LOGGER.info("Worker pool: {} workers, {}Mbps", workersAndExpiries.size(), getPoolBandwidthMegabitsPerSecond());
    }

}
