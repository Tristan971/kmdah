package moe.tristan.kmdah.service.vacuum;

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

    // once per hour
    @Scheduled(fixedDelay = 3600 * 1000)
    public void triggerVacuuming() {
        VacuumingRequest vacuumingRequest = new VacuumingRequest(DataSize.ofGigabytes(cacheSettings.maxSizeGb()));
        VacuumingResult vacuumingResult = cachedImageService.vacuum(vacuumingRequest);
        if (vacuumingResult.deletedFileCount() > 0) {
            LOGGER.info(
                "Vacuuming run done - freed {}MB by deleting {} files",
                vacuumingResult.freedSpace().toMegabytes(),
                vacuumingResult.deletedFileCount()
            );
        } else {
            LOGGER.info("Vacuuming run finished without any deletion");
        }
    }

}
