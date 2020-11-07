package moe.tristan.kmdah.operator.service.workers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.common.api.worker.Worker;
import moe.tristan.kmdah.common.api.worker.WorkerConfiguration;
import moe.tristan.kmdah.common.api.worker.WorkerShutdown;
import moe.tristan.kmdah.common.model.configuration.OperatorSettings;


@Service
public class WorkerPoolService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerPoolService.class);

    private final OperatorSettings operatorSettings;
    private final WorkerConfigurationHolder workerConfigurationHolder;
    private final Map<Worker, Instant> workersAndExpiries = new ConcurrentHashMap<>();

    public WorkerPoolService(OperatorSettings operatorSettings, WorkerConfigurationHolder workerConfigurationHolder) {
        this.operatorSettings = operatorSettings;
        this.workerConfigurationHolder = workerConfigurationHolder;
    }

    public WorkerConfiguration heartbeat(Worker worker) {
        Instant previous = workersAndExpiries.put(worker, Instant.now().plus(30, ChronoUnit.SECONDS));

        if (previous == null) {
            LOGGER.info("Successfully registered worker: {}", worker.getUniqueName());
            logPoolStatus();
        } else {
            LOGGER.debug("Heartbeat received from worker: {}", worker);
        }

        return WorkerConfiguration.of(workerConfigurationHolder.getImageServer());
    }

    public WorkerShutdown disconnect(Worker worker) {
        workersAndExpiries.remove(worker);
        LOGGER.info("Successfully unregistered worker: {}", worker);
        logPoolStatus();

        OptionalInt workerGracefulShutdownSeconds =
            workersAndExpiries.isEmpty()
            ? OptionalInt.of(operatorSettings.getGracefulShutdownSeconds())
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
        }
    }

    public int getPoolBandwidthMbps() {
        return workersAndExpiries.keySet().stream().mapToInt(Worker::getBandwidthMbps).sum();
    }

    private void logPoolStatus() {
        LOGGER.info("Worker pool: {} workers, {}Mbps", workersAndExpiries.size(), getPoolBandwidthMbps());
    }

}
