package moe.tristan.kmdah.service.workers;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkersRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkersRegistry.class);

    private final ObjectMapper objectMapper;
    private final Map<String, Instant> knownWorkers;

    public WorkersRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.knownWorkers = new ConcurrentHashMap<>();
    }

    void registerWorker(String uuid) {
        Instant previous = knownWorkers.put(uuid, Instant.now());
        if (previous == null) {
            LOGGER.info("Registered worker [{}] (known workers: [{}])", uuid, knownWorkers.keySet());
        } else {
            LOGGER.info("Received heartbeat from [{}]", uuid);
        }
    }

    void unregisterWorker(String uuid) {
        Instant worker = knownWorkers.remove(uuid);
        if (worker == null) {
            LOGGER.warn("Tried unregistering [{}], but it wasn't registered!", uuid);
        } else {
            LOGGER.info("Unregistered [{}] (known workers: [{}])", uuid, knownWorkers.keySet());
        }
    }

}
