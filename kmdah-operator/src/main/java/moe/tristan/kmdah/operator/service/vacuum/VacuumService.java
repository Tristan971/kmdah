package moe.tristan.kmdah.operator.service.vacuum;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.unit.DataSize;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import io.micrometer.core.annotation.Timed;
import moe.tristan.kmdah.common.model.settings.S3Settings;
import moe.tristan.kmdah.operator.model.CacheSettings;
import moe.tristan.kmdah.operator.model.vacuum.BucketScanResult;

/**
 * Deletes stuff from the cache, 100-by-100, until we're under the cache size
 */
@Service
public class VacuumService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VacuumService.class);
    private static final Pattern IMAGE_KEY_PATTERN = Pattern.compile("^(data|data-saver)/.+/.+\\..+$");

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

        LOGGER.info("Starting bucket scan");
        DataSize limitSize = DataSize.ofGigabytes(cacheSettings.getMaxSizeGb());
        BucketScanResult preVacuumScan = scanBucket();

        double originalFill = getFillPercentage(preVacuumScan.getSize(), limitSize);
        LOGGER.info("Cache size usage: {}% - {}/{} GB", (int) originalFill, preVacuumScan.getSize().toGigabytes(), limitSize.toGigabytes());

        double percentageOfFilesToDelete = originalFill - 100.;
        int filesToDelete = preVacuumScan.getObjects().size() * (int) percentageOfFilesToDelete / 100;

        if (filesToDelete > 0) {
            LOGGER.info("Above size threshold, will vacuum {}% of files ({} files)", (int) percentageOfFilesToDelete, filesToDelete);
            List<S3ObjectSummary> filesDeleted = preVacuumScan
                .getObjects()
                .stream()
                .filter(summary -> IMAGE_KEY_PATTERN.matcher(summary.getKey()).matches())
                .limit(filesToDelete)
                .collect(Collectors.toList());

            long deletedSize = filesDeleted.stream().mapToLong(S3ObjectSummary::getSize).sum();
            List<KeyVersion> deletedKeys = filesDeleted.stream().map(S3ObjectSummary::getKey).map(KeyVersion::new).collect(Collectors.toList());

            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(s3Settings.getBucketName());
            deleteObjectsRequest.setKeys(deletedKeys);

            amazonS3.deleteObjects(deleteObjectsRequest);
            LOGGER.info("Done deleting {} random files - reclaimed {}", deletedKeys, deletedSize);
        } else {
            LOGGER.info("Under size threshold, no need to vacuum files");
            return;
        }

        BucketScanResult postVacuumSize = scanBucket();
        double postVacuumFill = getFillPercentage(postVacuumSize.getSize(), limitSize);

        stopWatch.stop();
        LOGGER.info(
            "Done shrinking cache ({}% -> {}%) in {}s",
            (int) originalFill,
            (int) postVacuumFill,
            stopWatch.getTotalTimeSeconds()
        );
    }

    private double getFillPercentage(DataSize current, DataSize limit) {
        return ((double) current.toBytes() / (double) limit.toBytes()) * 100.0;
    }

    private BucketScanResult scanBucket() {
        ObjectListing objectListing = amazonS3.listObjects(s3Settings.getBucketName());
        List<S3ObjectSummary> summaries = new ArrayList<>(objectListing.getObjectSummaries());
        LOGGER.info("Discovered {} files", summaries.size());
        do {
            summaries.addAll(objectListing.getObjectSummaries());
            LOGGER.info("Discovered {} files", summaries.size());
            objectListing = amazonS3.listNextBatchOfObjects(objectListing);
        } while (objectListing.isTruncated());

        LOGGER.info("Bucket contains {} objects", summaries.size());
        long bucketSize = summaries.stream().mapToLong(S3ObjectSummary::getSize).sum();

        return BucketScanResult.of(DataSize.ofBytes(bucketSize), summaries);
    }

    private boolean isImage(String key) {

    }

}
