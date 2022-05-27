package moe.tristan.kmdah.mangadex.ping;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.RestTemplate;

import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.mangadex.MangadexSettings;
import moe.tristan.kmdah.service.images.cache.CacheSettings;

@Service
public class PingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingService.class);

    private final RestTemplate restTemplate;

    private final MangadexApi mangadexApi;
    private final int mangadexSpecVersion;

    private final CacheSettings cacheSettings;
    private final MangadexSettings mangadexSettings;

    public PingService(
        RestTemplate restTemplate,
        MangadexApi mangadexApi,
        @Value("${spring.application.spec}") int mangadexSpecVersion,
        CacheSettings cacheSettings,
        MangadexSettings mangadexSettings
    ) {
        this.restTemplate = restTemplate;
        this.mangadexApi = mangadexApi;
        this.mangadexSpecVersion = mangadexSpecVersion;
        this.cacheSettings = cacheSettings;
        this.mangadexSettings = mangadexSettings;
    }

    public PingResponse ping(Optional<ZonedDateTime> lastCreatedAt, DataSize poolSpeed) {
        long networkSpeedBytesPerSecond = poolSpeed.toBytes();
        if (networkSpeedBytesPerSecond == 0L) {
            LOGGER.info("Worker pool is empty, requesting 1B/s network speed");
            networkSpeedBytesPerSecond = 1L;
        }

        PingRequest request = new PingRequest(
            mangadexSettings.clientSecret(),
            mangadexSettings.loadBalancerIp(),
            443,
            DataSize.ofGigabytes(cacheSettings.maxSizeGb()).toBytes(),
            networkSpeedBytesPerSecond,
            lastCreatedAt,
            mangadexSpecVersion
        );

        LOGGER.info("Ping {}", request);
        PingResponse response = restTemplate.postForObject(
            mangadexApi.getApiUrl() + "/ping",
            request,
            PingResponse.class
        );
        LOGGER.info("Pong {}", response);
        return response;
    }

}
