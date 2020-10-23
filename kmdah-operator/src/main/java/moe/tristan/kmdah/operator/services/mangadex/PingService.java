package moe.tristan.kmdah.operator.services.mangadex;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.common.mangadex.MangadexApi;
import moe.tristan.kmdah.common.mangadex.ping.PingRequest;
import moe.tristan.kmdah.common.mangadex.ping.PingResponse;
import moe.tristan.kmdah.operator.userconfig.UserNetworkSettings;
import moe.tristan.kmdah.operator.userconfig.UserRootSettings;
import moe.tristan.kmdah.operator.userconfig.UserStorageSettings;

import io.micrometer.core.annotation.Timed;

@Service
public class PingService {

    private static final URI PING_ENDPOINT = UriComponentsBuilder
        .fromHttpUrl(MangadexApi.BASE_URL)
        .path("/ping")
        .build()
        .toUri();

    private final RestTemplate restTemplate;

    private final UserRootSettings userRootSettings;
    private final UserStorageSettings userStorageSettings;
    private final UserNetworkSettings userNetworkSettings;

    public PingService(
        RestTemplate restTemplate,
        UserRootSettings userRootSettings,
        UserStorageSettings userStorageSettings,
        UserNetworkSettings userNetworkSettings
    ) {
        this.restTemplate = restTemplate;
        this.userRootSettings = userRootSettings;
        this.userStorageSettings = userStorageSettings;
        this.userNetworkSettings = userNetworkSettings;
    }

    @Timed
    public PingResponse ping(Optional<ZonedDateTime> lastCreatedAt) {
        PingRequest request = PingRequest
            .builder()
            .secret(userRootSettings.getSecret())
            .port(userNetworkSettings.getPort())
            .diskSpace(userStorageSettings.getCacheSizeMebibytes())
            .networkSpeed(userNetworkSettings.getBandwidthMegabytesPerSecond() * 1024)
            .tlsCreatedAt(lastCreatedAt)
            .build();

        try {
            return restTemplate.postForObject(
                PING_ENDPOINT,
                request,
                PingResponse.class
            );
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new IllegalStateException("Unauthorized! Either your secret is wrong, or your server was marked as compromised!");
        } catch (HttpClientErrorException.UnsupportedMediaType e) {
            throw new IllegalStateException("Content-Type was not set to application/json");
        } catch (HttpClientErrorException.BadRequest e) {
            throw new IllegalStateException("Json body was malformed!");
        } catch (HttpClientErrorException.Forbidden e) {
            throw new IllegalStateException("Secret is not valid anymore!");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception!", e);
        }
    }

}
