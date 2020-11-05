package moe.tristan.kmdah.operator.service.workers;

import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.common.internal.Worker;

@Service
public class WorkerHealthService {

    public static final String TEST_IMAGE_PATH = "/data/a61fa9f7f1313194787116d1357a7784/N9.jpg";

    private final RestTemplate restTemplate;

    public WorkerHealthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void validateWorkerHealth(Worker worker) {
        URI workerTestUri = UriComponentsBuilder
            .fromHttpUrl(worker.getHttpUrl())
            .path(TEST_IMAGE_PATH)
            .build()
            .toUri();

        byte[] response = restTemplate.getForObject(workerTestUri, byte[].class);
        if (response == null || response.length == 0) {
            throw new IllegalStateException("Worker " + worker + " replied on test endpoint, but the response was null or empty!");
        }
    }

}
