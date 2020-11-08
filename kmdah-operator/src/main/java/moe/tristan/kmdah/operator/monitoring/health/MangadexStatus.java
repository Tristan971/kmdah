package moe.tristan.kmdah.operator.monitoring.health;

import java.util.Optional;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.common.model.mangadex.ping.PingResponse;
import moe.tristan.kmdah.operator.service.mangadex.MangadexLifecycle;

@Component
public class MangadexStatus implements HealthIndicator {

    private final MangadexLifecycle mangadexLifecycle;

    public MangadexStatus(MangadexLifecycle mangadexLifecycle) {
        this.mangadexLifecycle = mangadexLifecycle;
    }

    @Override
    public Health health() {
        PingResponse lastResponse = mangadexLifecycle.getLastPingResponse();

        Health.Builder health = Health
            .outOfService();

        Optional.ofNullable(lastResponse).ifPresentOrElse(data -> {
            health.withDetail("imageServer", data.getImageServer());
            health.withDetail("url", data.getUrl());
            health.withDetail("compromised", data.isCompromised());
            health.withDetail("paused", data.isPaused());
        }, () -> health.withDetail("pingResponse", "null"));

        // before first ping, last response is null
        if (lastResponse == null) {
            return health.build();
        }

        if (lastResponse.isCompromised()) {
            return health
                .down()
                .withDetail("reason", "compromised")
                .build();
        }

        return health
            .up()
            .build();
    }

}
