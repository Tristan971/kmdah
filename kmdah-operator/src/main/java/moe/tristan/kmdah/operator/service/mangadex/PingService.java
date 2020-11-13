package moe.tristan.kmdah.operator.service.mangadex;

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

import io.micrometer.core.annotation.Timed;
import moe.tristan.kmdah.common.model.mangadex.MangadexApi;
import moe.tristan.kmdah.common.model.mangadex.ping.PingRequest;
import moe.tristan.kmdah.common.model.mangadex.ping.PingResponse;
import moe.tristan.kmdah.operator.model.CacheSettings;
import moe.tristan.kmdah.operator.model.MangadexSettings;
import moe.tristan.kmdah.operator.service.workers.WorkerPool;

@Service
public class PingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingService.class);

    private static final URI PING_ENDPOINT = UriComponentsBuilder
        .fromHttpUrl(MangadexApi.BASE_URL)
        .path("/ping")
        .build()
        .toUri();

    private final RestTemplate restTemplate;

    private final WorkerPool workerPool;
    private final CacheSettings cacheSettings;
    private final MangadexSettings mangadexSettings;

    public PingService(
        RestTemplate restTemplate,
        CacheSettings cacheSettings,
        MangadexSettings mangadexSettings,
        WorkerPool workerPool
    ) {
        this.restTemplate = restTemplate;
        this.cacheSettings = cacheSettings;
        this.mangadexSettings = mangadexSettings;
        this.workerPool = workerPool;
    }

    @Timed
    public PingResponse ping(Optional<ZonedDateTime> lastCreatedAt) {
        long poolSpeedMegabitsPerSecond = workerPool.getPoolBandwidthMegabitsPerSecond();
        long networkSpeedBytesPerSecond = poolSpeedMegabitsPerSecond * 1024 * 1024 / 8;
        if (poolSpeedMegabitsPerSecond == 0L) {
            LOGGER.warn("Trying to ping for an empty pool! Requesting 1B/s speed instead.");
            networkSpeedBytesPerSecond = 1L;
        }

        PingRequest request = PingRequest
            .builder()
            .secret(mangadexSettings.getClientSecret())
            .port(443)
            .diskSpace((long) (DataSize.ofGigabytes(cacheSettings.getMaxSizeGb()).toBytes() * 0.8)) // spring uses mebibytes for DataSize (good on them <3)
            .networkSpeed(networkSpeedBytesPerSecond)
            .tlsCreatedAt(lastCreatedAt)
            .specVersion(19)
            .build();
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
