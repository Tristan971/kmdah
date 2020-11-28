package moe.tristan.kmdah.service.sync.workers;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkersRegistry implements MessageListener {

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

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            WorkerEvent workerEvent = objectMapper.readValue(message.getBody(), WorkerEvent.class);
            WorkerEventType eventType = workerEvent.type();

            switch (eventType) {
                case PING -> registerWorker(workerEvent.id());
                case SHUTDOWN -> unregisterWorker(workerEvent.id());
                default -> throw new IllegalArgumentException("Unknown worker event type: " + eventType);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read worker event for message: " + new String(message.getBody()), e);
        }
    }

}
