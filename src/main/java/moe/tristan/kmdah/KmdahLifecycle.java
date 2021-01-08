package moe.tristan.kmdah;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.service.gossip.messages.pub.GossipPublisher;

@Component
public class KmdahLifecycle implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(KmdahLifecycle.class);

    private final GossipPublisher gossipPublisher;
    private final ScheduledExecutorService scheduledExecutorService;
    private final LockRegistryLeaderInitiator lockRegistryLeaderInitiator;

    private final AtomicReference<ScheduledFuture<?>> gossipPingJob = new AtomicReference<>();

    public KmdahLifecycle(
        GossipPublisher gossipPublisher,
        ScheduledExecutorService scheduledExecutorService,
        LockRegistryLeaderInitiator lockRegistryLeaderInitiator
    ) {
        this.gossipPublisher = gossipPublisher;
        this.scheduledExecutorService = scheduledExecutorService;
        this.lockRegistryLeaderInitiator = lockRegistryLeaderInitiator;
    }

    @Override
    public void start() {
        gossipPingJob.set(scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                gossipPublisher.broadcastPing();
            } catch (Throwable e) {
                LOGGER.error("Failed to broadcast self!", e);
            }
        }, 0, 5, TimeUnit.SECONDS));
    }

    @Override
    public void stop() {
        // app gives up leadership itself, to ensure that it happens before the
        // redis connection is closed (in which case it might not communicate
        // itself going down, or give up the lock directly)
        lockRegistryLeaderInitiator.stop();
        Optional.ofNullable(gossipPingJob.get()).ifPresent(disposable -> {
            disposable.cancel(false);
            gossipPublisher.broadcastShutdown();
        });
    }

    @Override
    public boolean isRunning() {
        return Optional
            .ofNullable(gossipPingJob.get()) // was scheduled
            .map(disposable -> !disposable.isDone()) // hasn't been cancelled/terminated
            .orElse(false);
    }

}
