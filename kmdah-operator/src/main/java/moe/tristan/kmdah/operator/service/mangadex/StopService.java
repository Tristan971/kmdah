package moe.tristan.kmdah.operator.service.mangadex;

import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.common.model.mangadex.MangadexApi;
import moe.tristan.kmdah.common.model.mangadex.stop.StopRequest;
import moe.tristan.kmdah.operator.configuration.OperatorSettings;

import io.micrometer.core.annotation.Timed;

@Service
public class StopService {

    private static final URI STOP_ENDPOINT = UriComponentsBuilder
        .fromHttpUrl(MangadexApi.BASE_URL)
        .path("/stop")
        .build()
        .toUri();

    private final OperatorSettings operatorSettings;
    private final RestTemplate restTemplate;

    public StopService(OperatorSettings operatorSettings, RestTemplate restTemplate) {
        this.operatorSettings = operatorSettings;
        this.restTemplate = restTemplate;
    }

    @Timed
    public void stop() {
        restTemplate.postForObject(
            STOP_ENDPOINT,
            StopRequest.of(operatorSettings.getSecret()),
            Void.class
        );
    }

}
