package moe.tristan.kmdah.service.leader.heartbeat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import moe.tristan.kmdah.mangadex.ping.PingService;
import moe.tristan.kmdah.mangadex.ping.TlsData;
import moe.tristan.kmdah.mangadex.stop.StopService;
import moe.tristan.kmdah.service.gossip.messages.pub.GossipPublisher;
import moe.tristan.kmdah.service.kubernetes.TlsDataReceivedEvent;
import moe.tristan.kmdah.service.leader.LeaderActivity;
import moe.tristan.kmdah.service.workers.WorkersRegistry;

@Component
public class MangadexHeartbeatJob implements LeaderActivity {

    private final PingService pingService;
    private final StopService stopService;
    private final GossipPublisher gossipPublisher;
    private final WorkersRegistry workersRegistry;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final AtomicReference<LocalDateTime> lastCreatedAt = new AtomicReference<>();

    public MangadexHeartbeatJob(
        PingService pingService,
        StopService stopService,
        GossipPublisher gossipPublisher,
        WorkersRegistry workersRegistry,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.pingService = pingService;
        this.stopService = stopService;
        this.gossipPublisher = gossipPublisher;
        this.workersRegistry = workersRegistry;
        this.applicationEventPublisher = applicationEventPublisher;
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
            Optional<TlsData> tlsData = response.tls();

            // store last-created-at
            tlsData
                .map(TlsData::createdAt)
                .ifPresent(lastCreatedAt::set);

            // broadcast event to trigger k8s SSL secret update
            tlsData.map(TlsDataReceivedEvent::new).ifPresent(applicationEventPublisher::publishEvent);

            gossipPublisher.broadcastImageServer(response.imageServer());
        });
    }

    @Override
    public void stop() {
        stopService.stop().subscribe();
    }

}
