package moe.tristan.kmdah.operator.service.mangadex;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.common.model.configuration.OperatorSettings;
import moe.tristan.kmdah.common.model.mangadex.ping.PingResponse;
import moe.tristan.kmdah.common.model.mangadex.ping.TlsData;

@Component
public class MangadexLifecycle implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MangadexLifecycle.class);

    private final OperatorSettings operatorSettings;
    private final StopService stopService;
    private final PingService pingService;
    private final TaskScheduler taskScheduler;

    private final AtomicReference<ZonedDateTime> lastCreatedAt = new AtomicReference<>();

    private ScheduledFuture<?> pingJob;
    private boolean running = false;

    public MangadexLifecycle(
        OperatorSettings operatorSettings,
        StopService stopService,
        PingService pingService,
        TaskScheduler taskScheduler
    ) {
        this.operatorSettings = operatorSettings;
        this.stopService = stopService;
        this.pingService = pingService;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void start() {
        pingJob = taskScheduler.scheduleWithFixedDelay(() -> {
            PingResponse pingResponse = pingService.ping(Optional.ofNullable(lastCreatedAt.get()));
            ZonedDateTime createdAt = pingResponse.getTls().map(TlsData::getCreatedAt).orElseGet(lastCreatedAt::get);
            lastCreatedAt.set(createdAt);
        }, operatorSettings.getPingFrequencySeconds() * 1000L);
        running = true;
    }

    @Override
    public void stop() {
        try {
            pingJob.cancel(true);
            stopService.stop();
            Thread.sleep(operatorSettings.getGracefulShutdownSeconds() * 1000L);
        } catch (Exception e) {
            LOGGER.error("Failed graceful shutdown!", e);
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

}
