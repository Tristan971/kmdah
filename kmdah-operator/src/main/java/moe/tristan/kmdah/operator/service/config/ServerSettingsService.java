package moe.tristan.kmdah.operator.service.config;

import org.springframework.stereotype.Service;

import moe.tristan.kmdah.common.mangadex.ping.PingResponse;
import moe.tristan.kmdah.operator.service.mangadex.PingService;

@Service
public class ServerSettingsService {

    private final PingService pingService;

    private PingResponse lastPingResponse;

    public ServerSettingsService(PingService pingService) {
        this.pingService = pingService;
    }

}
