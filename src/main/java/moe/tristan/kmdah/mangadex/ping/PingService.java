package moe.tristan.kmdah.mangadex.ping;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.model.settings.CacheSettings;
import moe.tristan.kmdah.model.settings.MangadexSettings;
import reactor.core.publisher.Mono;

@Service
public class PingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingService.class);

    private static final URI PING_ENDPOINT = UriComponentsBuilder
        .fromHttpUrl(MangadexApi.BASE_URL)
        .path("/ping")
        .build()
        .toUri();

    private final WebClient webClient;

    private final CacheSettings cacheSettings;
    private final MangadexSettings mangadexSettings;

    public PingService(
        WebClient.Builder webClient,
        CacheSettings cacheSettings,
        MangadexSettings mangadexSettings
    ) {
        this.webClient = webClient.build();
        this.cacheSettings = cacheSettings;
        this.mangadexSettings = mangadexSettings;
    }

    public Mono<PingResponse> ping(Optional<ZonedDateTime> lastCreatedAt, DataSize poolSpeed) {
        long networkSpeedBytesPerSecond = poolSpeed.toBytes();
        if (networkSpeedBytesPerSecond == 0L) {
            LOGGER.warn("Trying to ping for an empty pool! Requesting 1B/s speed instead.");
            networkSpeedBytesPerSecond = 1L;
        }

        PingRequest request = new PingRequest(
            mangadexSettings.getClientSecret(),
            443,
            (long) (DataSize.ofGigabytes(cacheSettings.getMaxSizeGb()).toBytes() * 0.8),
            networkSpeedBytesPerSecond,
            lastCreatedAt,
            19
        );

        LOGGER.info("{}", request);

        return webClient
            .post()
            .uri(PING_ENDPOINT)
            .retrieve()
            .onStatus(status -> HttpStatus.OK != status, this::onError)
            .bodyToMono(PingResponse.class);
    }

    private Mono<? extends Throwable> onError(ClientResponse clientResponse) {
        return clientResponse
            .createException()
            .map(error -> switch (clientResponse.statusCode()) {
                case UNAUTHORIZED -> new IllegalStateException(
                    "Unauthorized! Either your secret is wrong, or your server was marked as compromised!", error
                );

                case UNSUPPORTED_MEDIA_TYPE -> new IllegalStateException(
                    "Content-Type was not set to application/json", error
                );

                case BAD_REQUEST -> new IllegalStateException(
                    "Json body was malformed!", error
                );

                case FORBIDDEN -> new IllegalStateException(
                    "Secret is not valid anymore!", error
                );

                default -> new RuntimeException(
                    "Unexpected server error response!", error
                );
            });
    }

}
