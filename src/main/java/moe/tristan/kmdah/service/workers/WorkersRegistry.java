package moe.tristan.kmdah.service.workers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.service.gossip.InstanceId;
import moe.tristan.kmdah.service.gossip.messages.WorkerPingEvent;
import moe.tristan.kmdah.service.gossip.messages.WorkerShutdownEvent;

@Component
public class WorkersRegistry implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkersRegistry.class);

    private final InstanceId instanceId;
    private final Map<WorkerInfo, Instant> knownWorkers;

    public WorkersRegistry(InstanceId instanceId) {
        this.instanceId = instanceId;
        this.knownWorkers = new ConcurrentHashMap<>();
    }

    public long getTotalBandwidthMbps() {
        return knownWorkers
            .keySet()
            .stream()
            .mapToLong(WorkerInfo::bandwidthMbps)
            .sum();
    }

    public long getOtherWorkersCount() {
        return knownWorkers
            .keySet()
            .stream()
            .filter(workerInfo -> !instanceId.id().equals(workerInfo.id()))
            .count();
    }

    @EventListener(WorkerPingEvent.class)
    public void registerWorker(WorkerPingEvent pingEvent) {
        Instant previous = knownWorkers.put(pingEvent.worker(), Instant.now());
        if (previous == null) {
            LOGGER.info("Registered worker [{}]", pingEvent.worker().id());
            logWorkersState();
        } else {
            LOGGER.debug("Received heartbeat from [{}]", pingEvent.worker().id());
        }
    }

    @EventListener(WorkerShutdownEvent.class)
    public void unregisterWorker(WorkerShutdownEvent shutdownEvent) {
        Instant worker = knownWorkers.remove(shutdownEvent.worker());
        if (worker == null) {
            LOGGER.warn("Tried unregistering [{}], but it wasn't registered!", shutdownEvent.worker().id());
        } else {
            LOGGER.info("Unregistered [{}]", shutdownEvent.worker().id());
            logWorkersState();
        }
    }

    void logWorkersState() {
        List<String> workerList = knownWorkers.keySet().stream().map(WorkerInfo::id).sorted().collect(Collectors.toList());
        long totalBandwidthMbps = getTotalBandwidthMbps();
        LOGGER.info("Worker registry now contains: {} for a total of {}Mbps of bandwidth", workerList, totalBandwidthMbps);
    }

    @Override
    public Health health() {
        if (knownWorkers.size() == 0) {
            return Health
                .outOfService()
                .withDetail("reason", "No other workers known, potentially not in sync with leader")
                .build();
        } else {
            return Health.up().build();
        }
    }

}
