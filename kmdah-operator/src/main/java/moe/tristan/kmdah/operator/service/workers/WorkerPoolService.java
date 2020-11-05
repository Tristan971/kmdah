package moe.tristan.kmdah.operator.service.workers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.common.internal.Worker;
import moe.tristan.kmdah.common.internal.WorkerConfig;
import moe.tristan.kmdah.common.internal.WorkerShutdownConfig;

@Service
public class WorkerPoolService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerPoolService.class);

    private static final ScheduledExecutorService WORKER_REAPER = Executors.newSingleThreadScheduledExecutor();

    private final WorkerHealthService workerHealthService;

    private final Map<Worker, Instant> workersAndExpiries = new ConcurrentHashMap<>();

    public WorkerPoolService(WorkerHealthService workerHealthService) {
        this.workerHealthService = workerHealthService;
        WORKER_REAPER.scheduleAtFixedRate(this::reapExpired, 1, 5, TimeUnit.SECONDS);
    }

    public WorkerConfig heartbeat(Worker worker) {
        workerHealthService.validateWorkerHealth(worker);

        Instant previous = workersAndExpiries.put(worker, Instant.now().plus(1, ChronoUnit.MINUTES));
        if (previous == null) {
            LOGGER.info("Successfully registered worker: {}", worker);
        } else {
            LOGGER.info("Heartbeat received from worker: {}", worker);
        }

        return WorkerConfig.of("https://s2.mangadex.org"); // todo: use the one from ping response
    }

    public WorkerShutdownConfig disconnect(Worker worker) {
        workersAndExpiries.remove(worker);
        LOGGER.info("Successfully unregistered worker: {}", worker);

        int requestedGracefulShutdownPeriodSeconds = 0;
        if (workersAndExpiries.isEmpty()) {
            requestedGracefulShutdownPeriodSeconds = 60 * 5; // if this is the latest worker, request that it waits 5 minutes to shutdown
        }

        return WorkerShutdownConfig.of(requestedGracefulShutdownPeriodSeconds);
    }

    private void reapExpired() {
        Instant now = Instant.now();

        Set<Worker> expiredWorkers = workersAndExpiries
            .keySet()
            .stream()
            .filter(worker -> workersAndExpiries.get(worker).isBefore(now))
            .collect(Collectors.toSet());

        if (!expiredWorkers.isEmpty()) {
            LOGGER.error("Unregistering expired workers: {}", expiredWorkers);
        }

        expiredWorkers.forEach(workersAndExpiries::remove);
    }

    public int getPoolBandwidthMbps() {
        return workersAndExpiries.keySet().stream().mapToInt(Worker::getBandwidthMbps).sum();
    }

}
