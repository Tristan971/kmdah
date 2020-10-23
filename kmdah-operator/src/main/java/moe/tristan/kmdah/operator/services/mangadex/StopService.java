package moe.tristan.kmdah.operator.services.mangadex;

import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.common.mangadex.MangadexApi;
import moe.tristan.kmdah.common.mangadex.stop.StopRequest;
import moe.tristan.kmdah.operator.userconfig.UserRootSettings;

import io.micrometer.core.annotation.Timed;

@Service
public class StopService {

    private static final URI STOP_ENDPOINT = UriComponentsBuilder
        .fromHttpUrl(MangadexApi.BASE_URL)
        .path("/stop")
        .build()
        .toUri();

    private final RestTemplate restTemplate;
    private final UserRootSettings userRootSettings;

    public StopService(RestTemplate restTemplate, UserRootSettings userRootSettings) {
        this.restTemplate = restTemplate;
        this.userRootSettings = userRootSettings;
    }

    @Timed
    public void stop() {
        restTemplate.postForObject(
            STOP_ENDPOINT,
            StopRequest.of(userRootSettings.getSecret()),
            Void.class
        );
    }

}
