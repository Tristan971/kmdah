package moe.tristan.kmdah.mangadex.stop;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.model.settings.MangadexSettings;

@Service
public class StopService {

    private final WebClient webClient;
    private final MangadexApi mangadexApi;
    private final MangadexSettings mangadexSettings;

    public StopService(WebClient.Builder webClient, MangadexApi mangadexApi, MangadexSettings mangadexSettings) {
        this.webClient = webClient.build();
        this.mangadexApi = mangadexApi;
        this.mangadexSettings = mangadexSettings;
    }

    public void stop() {
        webClient
            .post()
            .uri("{api}/stop", mangadexApi.getApiUrl())
            .bodyValue(new StopRequest(mangadexSettings.clientSecret()))
            .retrieve()
            .toBodilessEntity();
    }

}
