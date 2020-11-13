package moe.tristan.kmdah.worker.service.lifecycle;

import java.net.Inet4Address;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.common.api.worker.Worker;
import moe.tristan.kmdah.common.api.worker.WorkerConfiguration;
import moe.tristan.kmdah.common.api.worker.WorkerShutdown;
import moe.tristan.kmdah.worker.model.WorkerSettings;

import io.micrometer.core.annotation.Timed;

@Service
public class HeartbeatService {

    private final RestTemplate restTemplate;

    private final URI operatorWorkersEndpoint;
    private final Worker self;

    public HeartbeatService(RestTemplate restTemplate, WorkerSettings workerSettings) throws UnknownHostException {
        this.restTemplate = restTemplate;
        this.operatorWorkersEndpoint = UriComponentsBuilder
            .fromUri(workerSettings.getOperatorUri())
            .path("/worker")
            .build()
            .toUri();
        String workerName = Optional.ofNullable(System.getenv("KMDAH_WORKER_NAME")).orElse(Inet4Address.getLocalHost().getHostName());
        this.self = Worker
            .builder()
            .uniqueName(workerName)
            .bandwidthMegabitsPerSecond(workerSettings.getBandwidthMbps())
            .build();
    }

    @Timed
    WorkerConfiguration heartbeat() {
        HttpEntity<Worker> entity = new HttpEntity<>(self);

        ResponseEntity<WorkerConfiguration> response = restTemplate.exchange(
            operatorWorkersEndpoint,
            HttpMethod.PUT,
            entity,
            WorkerConfiguration.class
        );
        return Objects.requireNonNull(response.getBody(), "Empty worker heartbeat response!");
    }

    @Timed
    WorkerShutdown shutdown() {
        HttpEntity<Worker> entity = new HttpEntity<>(self);

        ResponseEntity<WorkerShutdown> response = restTemplate.exchange(
            operatorWorkersEndpoint,
            HttpMethod.DELETE,
            entity,
            WorkerShutdown.class
        );
        return Objects.requireNonNull(response.getBody(), "Empty worker shutdown response!");
    }

}
