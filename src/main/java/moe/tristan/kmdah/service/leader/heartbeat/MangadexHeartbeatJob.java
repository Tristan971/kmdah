package moe.tristan.kmdah.service.leader.heartbeat;

import java.time.Duration;

import moe.tristan.kmdah.mangadex.ping.PingService;
import moe.tristan.kmdah.service.leader.LeaderActivity;
import moe.tristan.kmdah.service.workers.WorkersRegistry;

public class MangadexHeartbeatJob implements LeaderActivity {

    private final PingService pingService;
    private final WorkersRegistry workersRegistry;

    public MangadexHeartbeatJob(PingService pingService, WorkersRegistry workersRegistry) {
        this.pingService = pingService;
        this.workersRegistry = workersRegistry;
    }

    @Override
    public String getName() {
        return "Mangadex heartbeat";
    }

    @Override
    public Duration getInitialDelay() {
        return Duration.ZERO;
    }

    @Override
    public Duration getPeriod() {
        return Duration.ofSeconds(10);
    }

    @Override
    public void run() {

    }

}
