package moe.tristan.kmdah.mangadex.ping;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.HttpClientErrorException.Forbidden;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;
import org.springframework.web.client.HttpClientErrorException.UnsupportedMediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.model.settings.CacheSettings;
import moe.tristan.kmdah.model.settings.MangadexSettings;

@Service
public class PingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingService.class);

    private static final URI PING_ENDPOINT = UriComponentsBuilder
        .fromHttpUrl(MangadexApi.BASE_URL)
        .path("/ping")
        .build()
        .toUri();

    private final RestTemplate restTemplate;

    private final CacheSettings cacheSettings;
    private final MangadexSettings mangadexSettings;

    public PingService(
        RestTemplate restTemplate,
        CacheSettings cacheSettings,
        MangadexSettings mangadexSettings
    ) {
        this.restTemplate = restTemplate;
        this.cacheSettings = cacheSettings;
        this.mangadexSettings = mangadexSettings;
    }

    public PingResponse ping(Optional<ZonedDateTime> lastCreatedAt) {
        long poolSpeedMegabitsPerSecond = 300;
        long networkSpeedBytesPerSecond = poolSpeedMegabitsPerSecond * 1024 * 1024 / 8;
        if (poolSpeedMegabitsPerSecond == 0L) {
            LOGGER.warn("Trying to ping for an empty pool! Requesting 1B/s speed instead.");
            networkSpeedBytesPerSecond = 1L;
        }

        PingRequest request = new PingRequest(
            mangadexSettings.clientSecret(),
            443,
            (long) (DataSize.ofGigabytes(cacheSettings.maxSizeGb()).toBytes() * 0.8),
            networkSpeedBytesPerSecond,
            lastCreatedAt,
            19
        );

        LOGGER.info("{}", request);

        try {
            PingResponse pingResponse = restTemplate.postForObject(
                PING_ENDPOINT,
                request,
                PingResponse.class
            );
            LOGGER.info("{}", pingResponse);
            return pingResponse;
        } catch (Unauthorized e) {
            throw new IllegalStateException("Unauthorized! Either your secret is wrong, or your server was marked as compromised!");
        } catch (UnsupportedMediaType e) {
            throw new IllegalStateException("Content-Type was not set to application/json");
        } catch (BadRequest e) {
            throw new IllegalStateException("Json body was malformed!");
        } catch (Forbidden e) {
            throw new IllegalStateException("Secret is not valid anymore!");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception!", e);
        }
    }

}
