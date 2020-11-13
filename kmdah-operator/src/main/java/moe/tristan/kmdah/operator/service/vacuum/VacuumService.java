package moe.tristan.kmdah.operator.service.vacuum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.unit.DataSize;

import io.micrometer.core.annotation.Timed;
import moe.tristan.kmdah.operator.model.CacheSettings;

/**
 * Deletes stuff from the cache, 100-by-100, until we're under the cache size
 */
@Service
public class VacuumService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VacuumService.class);

    private final CacheSettings cacheSettings;

    public VacuumService(CacheSettings cacheSettings) {
        this.cacheSettings = cacheSettings;
    }

    @Timed
    protected void vacuumUntilUnderCacheMaxSize() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        DataSize cacheSize = getCacheSize();
        LOGGER.info("Cache size usage: {}/{} GB", cacheSize.toGigabytes(), cacheSettings.getMaxSizeGb());

        double vacuumStartFillPercentage = getFillPercentage();
        if (vacuumStartFillPercentage <= 100.0) {
            LOGGER.info("No need for cache vacuuming ({}% full)", (int) vacuumStartFillPercentage);
            return;
        }

        long vacuumed = 0; // vacuum(vacuumStartFillPercentage - 100.0);
        double vacuumEndFillPercentage = getFillPercentage();

        stopWatch.stop();
        LOGGER.info(
            "Done shrinking cache ({}% -> {}%) - evicted {} chapters in {}s",
            (int) vacuumStartFillPercentage,
            (int) vacuumEndFillPercentage,
            vacuumed,
            stopWatch.getTotalTimeSeconds()
        );
    }

    private DataSize getCacheSize() {
        return DataSize.ofBytes(0L);
    }

    private double getFillPercentage() {
        long currentCacheSizeBytes = getCacheSize().toBytes();
        long maxCacheSizeBytes = DataSize.ofGigabytes(cacheSettings.getMaxSizeGb()).toBytes();

        return ((double) currentCacheSizeBytes / (double) maxCacheSizeBytes) * 100.0;
    }

}
