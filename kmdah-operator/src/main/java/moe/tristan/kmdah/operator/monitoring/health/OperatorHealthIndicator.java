package moe.tristan.kmdah.operator.monitoring.health;

import java.util.Optional;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.common.model.mangadex.ping.PingResponse;
import moe.tristan.kmdah.operator.service.mangadex.PingResponseReceivedEvent;

@Component
public class OperatorHealthIndicator implements HealthIndicator {

    private PingResponse lastPingResponse;

    @Override
    public Health health() {
        Health.Builder health = Health.up();

        Optional.ofNullable(lastPingResponse).ifPresentOrElse(data -> {
            health.withDetail("imageServer", data.getImageServer());
            health.withDetail("url", data.getUrl());
            health.withDetail("compromised", data.isCompromised());
            health.withDetail("paused", data.isPaused());
        }, () -> health.withDetail("pingResponse", "absent"));

        return health.build();
    }

    @EventListener(PingResponseReceivedEvent.class)
    public void setLastPingResponse(PingResponseReceivedEvent event) {
        this.lastPingResponse = event.getPingResponse();
    }

}
