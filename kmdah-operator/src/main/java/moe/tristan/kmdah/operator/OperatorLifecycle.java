package moe.tristan.kmdah.operator;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.common.model.mangadex.ping.PingResponse;
import moe.tristan.kmdah.common.model.mangadex.ping.TlsData;
import moe.tristan.kmdah.operator.model.MangadexSettings;
import moe.tristan.kmdah.operator.service.mangadex.PingResponseReceivedEvent;
import moe.tristan.kmdah.operator.service.mangadex.PingService;
import moe.tristan.kmdah.operator.service.mangadex.StopService;
import moe.tristan.kmdah.operator.service.workers.WorkerPoolEmptiedEvent;
import moe.tristan.kmdah.operator.service.workers.WorkerRegisteredEvent;

@Component
public class OperatorLifecycle implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperatorLifecycle.class);

    private final TaskScheduler taskScheduler;
    private final PingService pingService;
    private final StopService stopService;
    private final MangadexSettings mangadexSettings;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final AtomicReference<OperatorStatus> status = new AtomicReference<>(OperatorStatus.INITIAL);
    private final AtomicReference<ZonedDateTime> lastCreatedAt = new AtomicReference<>();

    private ScheduledFuture<?> heartbeatJob;

    public OperatorLifecycle(
        TaskScheduler taskScheduler,
        MangadexSettings mangadexSettings,
        PingService pingService,
        StopService stopService,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.taskScheduler = taskScheduler;
        this.mangadexSettings = mangadexSettings;
        this.pingService = pingService;
        this.stopService = stopService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void start() {
        status.set(OperatorStatus.INITIAL);
    }

    @EventListener(WorkerRegisteredEvent.class)
    public void scaleUpWorkers() {
        OperatorStatus currentStatus = status.get();

        switch (currentStatus) {
            case INITIAL -> {
                status.set(OperatorStatus.STARTED);
                LOGGER.info("Starting heartbeat");
                startHeartbeat();
            }
            case STARTED -> LOGGER.info("Heartbeat already running - updating required network speed");
            case EXITING -> LOGGER.info("Operator is exiting already - ignoring");
        }
    }

    @EventListener(WorkerPoolEmptiedEvent.class)
    public void scaledDownWorkers() {
        LOGGER.info("Pool is empty, stopping operator heartbeat.");
        stopHeartbeat();
        status.set(OperatorStatus.INITIAL);
    }

    @Override
    public void stop() {
        if (OperatorStatus.INITIAL.equals(status.get())) {
            status.set(OperatorStatus.EXITED);
            return;
        }

        LOGGER.info("Preparing to exit operator.");
        status.set(OperatorStatus.EXITING);

        stopHeartbeat();
        try {
            LOGGER.info("Gracefully exiting - waiting {} seconds", mangadexSettings.getGracefulShutdownSeconds());
            Thread.sleep(mangadexSettings.getGracefulShutdownSeconds() * 1000L);
        } catch (InterruptedException e) {
            LOGGER.error("Error while waiting for graceful shutdown!", e);
        }

        status.set(OperatorStatus.EXITED);
        LOGGER.info("Operator is now safe to exit.");
    }

    @Override
    public boolean isRunning() {
        return status.get() != OperatorStatus.EXITED;
    }

    private synchronized void startHeartbeat() {
        if (heartbeatJob != null) {
            LOGGER.info("Heartbeat already running.");
            return;
        }

        LOGGER.info("Starting mangadex ping job - schedule: every 5 seconds");
        this.heartbeatJob = taskScheduler.scheduleWithFixedDelay(() -> {
            try {
                PingResponse pingResponse = pingService.ping(Optional.ofNullable(lastCreatedAt.get()));
                applicationEventPublisher.publishEvent(PingResponseReceivedEvent.of(pingResponse));

                ZonedDateTime createdAt = pingResponse.getTls().map(TlsData::getCreatedAt).orElseGet(lastCreatedAt::get);
                lastCreatedAt.set(createdAt);
            } catch (Throwable e) {
                LOGGER.error("Ping failure!", e);
            }
        }, 5000L);
    }

    private synchronized void stopHeartbeat() {
        if (heartbeatJob != null) {
            heartbeatJob.cancel(true);
            LOGGER.info("Stopped ping job");
            stopService.stop();
            LOGGER.info("Notified backend of shutdown.");
            heartbeatJob = null;
        }
    }

}
