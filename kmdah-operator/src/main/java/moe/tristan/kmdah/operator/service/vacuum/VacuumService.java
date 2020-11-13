package moe.tristan.kmdah.operator.service.vacuum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.unit.DataSize;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import io.micrometer.core.annotation.Timed;
import moe.tristan.kmdah.common.model.settings.S3Settings;
import moe.tristan.kmdah.operator.model.CacheSettings;

/**
 * Deletes stuff from the cache, 100-by-100, until we're under the cache size
 */
@Service
public class VacuumService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VacuumService.class);

    private final AmazonS3 amazonS3;
    private final S3Settings s3Settings;
    private final CacheSettings cacheSettings;

    public VacuumService(AmazonS3 amazonS3, S3Settings s3Settings, CacheSettings cacheSettings) {
        this.amazonS3 = amazonS3;
        this.s3Settings = s3Settings;
        this.cacheSettings = cacheSettings;
    }

    @Timed
    protected void vacuumUntilUnderCacheMaxSize() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        DataSize originalSize = getCacheSize();
        LOGGER.info("Cache size usage: {}/{} GB", originalSize.toGigabytes(), cacheSettings.getMaxSizeGb());

        double vacuumStartFillPercentage = getFillPercentage(originalSize);
        if (vacuumStartFillPercentage <= 100.0) {
            LOGGER.info("No need for cache vacuuming ({}% full)", (int) vacuumStartFillPercentage);
            return;
        }

        long vacuumed = 0; // vacuum(vacuumStartFillPercentage - 100.0);
        double vacuumEndFillPercentage = getFillPercentage(getCacheSize());

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
        ObjectListing objectListing = amazonS3.listObjects(s3Settings.getBucketName());
        long bucketSize = objectListing.getObjectSummaries().stream().mapToLong(S3ObjectSummary::getSize).sum();
        return DataSize.ofBytes(bucketSize);
    }

    private double getFillPercentage(DataSize current) {
        long maxCacheSizeBytes = DataSize.ofGigabytes(cacheSettings.getMaxSizeGb()).toBytes();

        return ((double) current.toBytes() / (double) maxCacheSizeBytes) * 100.0;
    }

}
