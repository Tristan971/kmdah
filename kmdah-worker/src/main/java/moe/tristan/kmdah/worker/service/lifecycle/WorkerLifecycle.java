package moe.tristan.kmdah.worker.service.lifecycle;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.common.api.worker.WorkerConfiguration;
import moe.tristan.kmdah.common.api.worker.WorkerShutdown;
import moe.tristan.kmdah.common.model.configuration.WorkerSettings;

@Component
public class WorkerLifecycle implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerLifecycle.class);

    private final TaskScheduler taskScheduler;
    private final WorkerSettings workerSettings;
    private final HeartbeatService heartbeatService;
    private final WorkerConfigurationService workerConfigurationService;

    private ScheduledFuture<?> heartbeatJob;
    private boolean running = false;

    public WorkerLifecycle(
        TaskScheduler taskScheduler,
        WorkerSettings workerSettings,
        HeartbeatService heartbeatService,
        WorkerConfigurationService workerConfigurationService
    ) {
        this.taskScheduler = taskScheduler;
        this.workerSettings = workerSettings;
        this.heartbeatService = heartbeatService;
        this.workerConfigurationService = workerConfigurationService;
    }

    @Override
    public void start() {
        doHeartbeat();
        heartbeatJob = taskScheduler.scheduleWithFixedDelay(this::doHeartbeat, 10_000);
        running = true;
    }

    @Override
    public void stop() {
        try {
            heartbeatJob.cancel(true);
            LOGGER.info("Stopping operator heartbeat job");

            WorkerShutdown shutdown = heartbeatService.shutdown();
            LOGGER.info("Notified the operator of incoming shutdown");

            if (shutdown.getGracefulShutdownSeconds().isPresent()) {
                int seconds = shutdown.getGracefulShutdownSeconds().getAsInt();
                LOGGER.info("Operator required graceful shutdown. Waiting {} seconds", seconds);
                Thread.sleep(seconds * 1000L);
            }
        } catch (InterruptedException e) {
            LOGGER.error("Failed to gracefully stop the worker!", e);
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void doHeartbeat() {
        WorkerConfiguration configuration = heartbeatService.heartbeat();
        workerConfigurationService.setImageServer(configuration.getImageServer());
    }

}
