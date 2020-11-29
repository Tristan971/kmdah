package moe.tristan.kmdah.service.workers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.service.gossip.GossipMessage;

@Component
public class WorkersRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkersRegistry.class);

    private final Map<WorkerInfo, Instant> knownWorkers;

    public WorkersRegistry() {
        this.knownWorkers = new ConcurrentHashMap<>();
    }

    public long getTotalBandwidthMbps() {
        return knownWorkers
            .keySet()
            .stream()
            .mapToLong(WorkerInfo::bandwidthMbps)
            .sum();
    }

    @EventListener(GossipMessage.class)
    public void receiveGossip(GossipMessage gossipMessage) {
        switch (gossipMessage.type()) {
            case PING -> registerWorker(gossipMessage.worker());
            case SHUTDOWN -> unregisterWorker(gossipMessage.worker());
        }
    }

    void registerWorker(WorkerInfo workerInfo) {
        Instant previous = knownWorkers.put(workerInfo, Instant.now());
        if (previous == null) {
            LOGGER.info("Registered worker [{}]", workerInfo.id());
            logWorkersState();
        } else {
            LOGGER.info("Received heartbeat from [{}]", workerInfo.id());
        }
    }

    void unregisterWorker(WorkerInfo workerInfo) {
        Instant worker = knownWorkers.remove(workerInfo);
        if (worker == null) {
            LOGGER.warn("Tried unregistering [{}], but it wasn't registered!", workerInfo.id());
        } else {
            LOGGER.info("Unregistered [{}]", workerInfo.id());
            logWorkersState();
        }
    }

    void logWorkersState() {
        List<String> workerList = knownWorkers.keySet().stream().map(WorkerInfo::id).sorted().collect(Collectors.toList());
        long totalBandwidthMbps = getTotalBandwidthMbps();
        LOGGER.info("Worker registry now contains: {} for a total of {}Mbps of bandwidth", workerList, totalBandwidthMbps);
    }

}
