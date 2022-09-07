package moe.tristan.kmdah.mangadex.stop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.mangadex.MangaDexSettings;

@Service
public class StopService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopService.class);

    private final RestTemplate restTemplate;
    private final MangadexApi mangadexApi;
    private final MangaDexSettings mangadexSettings;

    public StopService(RestTemplate restTemplate, MangadexApi mangadexApi, MangaDexSettings mangadexSettings) {
        this.restTemplate = restTemplate;
        this.mangadexApi = mangadexApi;
        this.mangadexSettings = mangadexSettings;
    }

    public ResponseEntity<Void> stop() {
        ResponseEntity<Void> response = restTemplate.postForEntity(
            mangadexApi.getApiUrl() + "/stop",
            new StopRequest(mangadexSettings.clientSecret()),
            Void.class
        );
        LOGGER.info("Stop request successful (acknowledged by backend: {})", response.getStatusCode());

        return response;
    }

}
