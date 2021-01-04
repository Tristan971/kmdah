package moe.tristan.kmdah;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import moe.tristan.kmdah.service.gossip.messages.pub.GossipPublisher;

@Component
public class KmdahLifecycle implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(KmdahLifecycle.class);

    private final GossipPublisher gossipPublisher;
    private final LockRegistryLeaderInitiator lockRegistryLeaderInitiator;

    private final Scheduler scheduler = Schedulers.boundedElastic();
    private final AtomicReference<Disposable> gossipPingJob = new AtomicReference<>();

    public KmdahLifecycle(
        GossipPublisher gossipPublisher,
        LockRegistryLeaderInitiator lockRegistryLeaderInitiator
    ) {
        this.gossipPublisher = gossipPublisher;
        this.lockRegistryLeaderInitiator = lockRegistryLeaderInitiator;
    }

    @Override
    public void start() {
        gossipPingJob.set(scheduler.schedulePeriodically(() -> {
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
            disposable.dispose();
            gossipPublisher.broadcastShutdown();
        });
    }

    @Override
    public boolean isRunning() {
        return Optional
            .ofNullable(gossipPingJob.get()) // was scheduled
            .map(disposable -> !disposable.isDisposed()) // hasn't been cancelled/terminated
            .orElse(false);
    }

}
