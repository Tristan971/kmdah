package moe.tristan.kmdah.mangadex.stop;

import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
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

    private final WebClient webClient;
    private final MangadexSettings mangadexSettings;

    public StopService(WebClient.Builder webClient, MangadexSettings mangadexSettings) {
        this.webClient = webClient.build();
        this.mangadexSettings = mangadexSettings;
    }

    public void stop() {
        webClient
            .post()
            .uri(STOP_ENDPOINT)
            .bodyValue(new StopRequest(mangadexSettings.clientSecret()))
            .retrieve()
            .toBodilessEntity();
    }

}
