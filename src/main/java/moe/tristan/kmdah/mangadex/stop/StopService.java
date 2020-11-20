package moe.tristan.kmdah.mangadex.stop;

import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.model.settings.MangadexSettings;

@Service
public class StopService {

    private static final URI STOP_ENDPOINT = UriComponentsBuilder
        .fromHttpUrl(MangadexApi.BASE_URL)
        .path("/stop")
        .build()
        .toUri();

    private final MangadexSettings mangadexSettings;
    private final RestTemplate restTemplate;

    public StopService(MangadexSettings mangadexSettings, RestTemplate restTemplate) {
        this.mangadexSettings = mangadexSettings;
        this.restTemplate = restTemplate;
    }

    public void stop() {
        restTemplate.postForObject(
            STOP_ENDPOINT,
            new StopRequest(mangadexSettings.clientSecret()),
            Void.class
        );
    }

}
