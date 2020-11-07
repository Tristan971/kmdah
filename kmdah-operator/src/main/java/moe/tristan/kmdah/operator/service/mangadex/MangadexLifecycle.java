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
import moe.tristan.kmdah.operator.service.workers.WorkerConfigurationHolder;

@Component
public class MangadexLifecycle implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MangadexLifecycle.class);

    private final TaskScheduler taskScheduler;

    private final PingService pingService;
    private final StopService stopService;
    private final OperatorSettings operatorSettings;
    private final WorkerConfigurationHolder workerConfigurationHolder;

    private final AtomicReference<ZonedDateTime> lastCreatedAt = new AtomicReference<>();

    private ScheduledFuture<?> pingJob;
    private boolean running = false;

    public MangadexLifecycle(
        TaskScheduler taskScheduler,
        PingService pingService,
        StopService stopService,
        OperatorSettings operatorSettings,
        WorkerConfigurationHolder workerConfigurationHolder
    ) {
        this.taskScheduler = taskScheduler;
        this.pingService = pingService;
        this.stopService = stopService;
        this.operatorSettings = operatorSettings;
        this.workerConfigurationHolder = workerConfigurationHolder;
    }

    @Override
    public void start() {
        LOGGER.info("Starting ping job, every {} seconds", operatorSettings.getPingFrequencySeconds());
        pingJob = taskScheduler.scheduleWithFixedDelay(this::doPing, operatorSettings.getPingFrequencySeconds() * 1000L);
        running = true;
    }

    @Override
    public void stop() {
        try {
            pingJob.cancel(true);
            LOGGER.info("Stopped ping job");

            stopService.stop();
            LOGGER.info("Notified backend of shutdown.");

            LOGGER.info("Awaiting for {} seconds before exiting.", operatorSettings.getGracefulShutdownSeconds());
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

    private void doPing() {
        PingResponse pingResponse = pingService.ping(Optional.ofNullable(lastCreatedAt.get()));
        ZonedDateTime createdAt = pingResponse.getTls().map(TlsData::getCreatedAt).orElseGet(lastCreatedAt::get);
        lastCreatedAt.set(createdAt);
        workerConfigurationHolder.setImageServer(pingResponse.getImageServer());
    }

}
