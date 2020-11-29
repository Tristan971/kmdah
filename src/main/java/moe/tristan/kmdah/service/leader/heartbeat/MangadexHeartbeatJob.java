package moe.tristan.kmdah.service.leader.heartbeat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import moe.tristan.kmdah.mangadex.ping.PingService;
import moe.tristan.kmdah.mangadex.ping.TlsData;
import moe.tristan.kmdah.service.gossip.messages.pub.GossipPublisher;
import moe.tristan.kmdah.service.leader.LeaderActivity;
import moe.tristan.kmdah.service.workers.WorkersRegistry;

@Component
public class MangadexHeartbeatJob implements LeaderActivity {

    private final PingService pingService;
    private final GossipPublisher gossipPublisher;
    private final WorkersRegistry workersRegistry;

    private final AtomicReference<LocalDateTime> lastCreatedAt = new AtomicReference<>();

    public MangadexHeartbeatJob(PingService pingService, GossipPublisher gossipPublisher, WorkersRegistry workersRegistry) {
        this.pingService = pingService;
        this.gossipPublisher = gossipPublisher;
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
        return Duration.ofSeconds(15);
    }

    @Override
    public void run() {
        pingService.ping(
            Optional.ofNullable(lastCreatedAt.get()),
            DataSize.ofMegabytes(workersRegistry.getTotalBandwidthMbps() / 8)
        ).subscribe(response -> {
            response
                .tls()
                .map(TlsData::createdAt)
                .ifPresent(lastCreatedAt::set);

            gossipPublisher.broadcastImageServer(response.imageServer());
        });
    }

}
