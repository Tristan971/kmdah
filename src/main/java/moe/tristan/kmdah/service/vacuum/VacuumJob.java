package moe.tristan.kmdah.service.vacuum;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import moe.tristan.kmdah.cache.CachedImageService;
import moe.tristan.kmdah.cache.VacuumingRequest;
import moe.tristan.kmdah.cache.VacuumingResult;
import moe.tristan.kmdah.model.settings.CacheSettings;

@Component
public class VacuumJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(VacuumJob.class);

    private final CacheSettings cacheSettings;
    private final CachedImageService cachedImageService;

    public VacuumJob(CacheSettings cacheSettings, CachedImageService cachedImageService) {
        this.cacheSettings = cacheSettings;
        this.cachedImageService = cachedImageService;
    }

    // once per hour, 3s initial delay
    @Scheduled(initialDelay = 5000, fixedDelay = 3600 * 1000)
    public void triggerVacuuming() {
        VacuumingRequest vacuumingRequest = new VacuumingRequest(DataSize.ofGigabytes(cacheSettings.maxSizeGb()));
        Optional<VacuumingResult> vacuumingResult = cachedImageService
            .vacuum(vacuumingRequest)
            .filter(result -> result.deletedFileCount() > 0)
            .blockOptional();

        if (vacuumingResult.isPresent()) {
            VacuumingResult result = vacuumingResult.get();
            LOGGER.info(
                "Vacuuming run done - freed {}MB by deleting {} files",
                result.freedSpace().toMegabytes(),
                result.deletedFileCount()
            );
        } else {
            LOGGER.info("Vacuuming run finished without any deletion");
        }
    }

}
