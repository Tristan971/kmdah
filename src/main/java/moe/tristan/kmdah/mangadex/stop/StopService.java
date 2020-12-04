package moe.tristan.kmdah.mangadex.stop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.mangadex.MangadexSettings;

@Service
public class StopService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopService.class);

    private final WebClient webClient;
    private final MangadexApi mangadexApi;
    private final MangadexSettings mangadexSettings;

    public StopService(WebClient.Builder webClient, MangadexApi mangadexApi, MangadexSettings mangadexSettings) {
        this.webClient = webClient.build();
        this.mangadexApi = mangadexApi;
        this.mangadexSettings = mangadexSettings;
    }

    public Mono<ResponseEntity<Void>> stop() {
        return webClient
            .post()
            .uri(mangadexApi.getApiUrl() + "/stop")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new StopRequest(mangadexSettings.clientSecret()))
            .retrieve()
            .toBodilessEntity()
            .doOnSuccess(res -> LOGGER.info("Stop request successful (acknowledged by backend: {})", res.getStatusCode()))
            .doOnError(err -> LOGGER.error("Stop request failed due to an error.", err));
    }

}
