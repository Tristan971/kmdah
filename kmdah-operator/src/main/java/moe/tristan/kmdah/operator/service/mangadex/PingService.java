package moe.tristan.kmdah.operator.service.mangadex;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.HttpClientErrorException.Forbidden;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;
import org.springframework.web.client.HttpClientErrorException.UnsupportedMediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.common.mangadex.MangadexApi;
import moe.tristan.kmdah.common.mangadex.ping.PingRequest;
import moe.tristan.kmdah.common.mangadex.ping.PingResponse;
import moe.tristan.kmdah.operator.configuration.OperatorSettings;
import moe.tristan.kmdah.operator.configuration.StorageSettings;
import moe.tristan.kmdah.operator.service.workers.WorkerPoolService;

import io.micrometer.core.annotation.Timed;

@Service
public class PingService {

    private static final URI PING_ENDPOINT = UriComponentsBuilder
        .fromHttpUrl(MangadexApi.BASE_URL)
        .path("/ping")
        .build()
        .toUri();

    private final RestTemplate restTemplate;

    private final OperatorSettings operatorSettings;
    private final StorageSettings storageSettings;

    private final WorkerPoolService workerPoolService;

    public PingService(
        RestTemplate restTemplate,
        OperatorSettings operatorSettings,
        StorageSettings storageSettings,
        WorkerPoolService workerPoolService
    ) {
        this.restTemplate = restTemplate;
        this.operatorSettings = operatorSettings;
        this.storageSettings = storageSettings;
        this.workerPoolService = workerPoolService;
    }

    @Timed
    public PingResponse ping(Optional<ZonedDateTime> lastCreatedAt) {
        PingRequest request = PingRequest
            .builder()
            .secret(operatorSettings.getSecret())
            .port(operatorSettings.getPort())
            .diskSpace(storageSettings.getCacheSizeMebibytes())
            .networkSpeed(workerPoolService.getPoolBandwidthMbps() * 1024)
            .tlsCreatedAt(lastCreatedAt)
            .build();

        try {
            return restTemplate.postForObject(
                PING_ENDPOINT,
                request,
                PingResponse.class
            );
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
