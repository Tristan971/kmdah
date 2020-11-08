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

import moe.tristan.kmdah.common.model.mangadex.ping.PingResponse;
import moe.tristan.kmdah.common.model.mangadex.ping.TlsData;
import moe.tristan.kmdah.common.model.settings.MangadexSettings;
import moe.tristan.kmdah.common.model.settings.OperatorSettings;
import moe.tristan.kmdah.operator.service.kubernetes.KubernetesIngressTlsSecretService;
import moe.tristan.kmdah.operator.service.workers.WorkerConfigurationHolder;

@Component
public class MangadexLifecycle implements SmartLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MangadexLifecycle.class);

    private final TaskScheduler taskScheduler;

    private final PingService pingService;
    private final StopService stopService;
    private final OperatorSettings operatorSettings;
    private final MangadexSettings mangadexSettings;
    private final WorkerConfigurationHolder workerConfigurationHolder;
    private final KubernetesIngressTlsSecretService kubernetesIngressTlsSecretService;


    private final AtomicReference<PingResponse> lastPingResponse = new AtomicReference<>();
    private final AtomicReference<ZonedDateTime> lastCreatedAt = new AtomicReference<>();

    private ScheduledFuture<?> pingJob;
    private boolean running = false;

    public MangadexLifecycle(
        TaskScheduler taskScheduler,
        PingService pingService,
        StopService stopService,
        OperatorSettings operatorSettings,
        MangadexSettings mangadexSettings,
        WorkerConfigurationHolder workerConfigurationHolder,
        KubernetesIngressTlsSecretService kubernetesIngressTlsSecretService
    ) {
        this.taskScheduler = taskScheduler;
        this.pingService = pingService;
        this.stopService = stopService;
        this.operatorSettings = operatorSettings;
        this.mangadexSettings = mangadexSettings;
        this.workerConfigurationHolder = workerConfigurationHolder;
        this.kubernetesIngressTlsSecretService = kubernetesIngressTlsSecretService;
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

            LOGGER.info("Awaiting for {} seconds before exiting.", mangadexSettings.getGracefulShutdownSeconds());
            Thread.sleep(mangadexSettings.getGracefulShutdownSeconds() * 1000L);
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
        lastPingResponse.set(pingResponse);

        // process createdAt for next ping calls
        ZonedDateTime createdAt = pingResponse.getTls().map(TlsData::getCreatedAt).orElseGet(lastCreatedAt::get);
        lastCreatedAt.set(createdAt);

        // ensure tlsdata is synchronized with k8s when updated by the backend
        pingResponse.getTls().ifPresent(kubernetesIngressTlsSecretService::syncTlsData);

        workerConfigurationHolder.setImageServer(pingResponse.getImageServer());
    }

    public PingResponse getLastPingResponse() {
        return lastPingResponse.get();
    }

}
