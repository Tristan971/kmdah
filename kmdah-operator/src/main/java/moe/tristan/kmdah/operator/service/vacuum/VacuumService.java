package moe.tristan.kmdah.operator.service.vacuum;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.unit.DataSize;

import moe.tristan.kmdah.common.model.settings.CacheSettings;

import io.micrometer.core.annotation.Timed;

/**
 * Deletes stuff from the cache, 100-by-100, until we're under the cache size
 */
@Service
public class VacuumService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VacuumService.class);

    private final Path cacheDirectoryPath;
    private final CacheSettings cacheSettings;
    private final ChapterDeleter chapterDeleter;

    public VacuumService(CacheSettings cacheSettings, ChapterDeleter chapterDeleter) {
        this.cacheSettings = cacheSettings;
        this.cacheDirectoryPath = Paths.get(cacheSettings.getRoot()).toAbsolutePath();
        this.chapterDeleter = chapterDeleter;
        LOGGER.info("Cache directory is: {}", cacheDirectoryPath);
    }

    @Timed
    protected void vacuumUntilUnderCacheMaxSize() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        DataSize cacheSize = getCacheSize();
        LOGGER.info("Cache size uage: {}/{} GB", cacheSize.toGigabytes(), cacheSettings.getSizeGib());

        double vacuumStartFillPercentage = getFillPercentage();
        if (vacuumStartFillPercentage <= 100.0) {
            LOGGER.info("No need for cache vacuuming ({}% full)", (int) vacuumStartFillPercentage);
            return;
        }

        long vacuumed = vacuum(vacuumStartFillPercentage - 100.0);
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

    private long vacuum(double overfullPercentage) {
        try (Stream<Path> chapterFoldersStream = Files.list(cacheDirectoryPath)) {
            List<Path> chapterFolders = chapterFoldersStream.collect(Collectors.toList());

            int totalChapterCount = chapterFolders.size();
            LOGGER.info("Number of chapters in cache {}", totalChapterCount);

            int chaptersToDeleteGuesstimate = (int) (totalChapterCount * (overfullPercentage / 100.0));
            LOGGER.info("Guesstimate recommends deleting {}% of chapters (overfill factor)", (int) overfullPercentage);

            Collections.shuffle(chapterFolders);
            chapterFolders
                .stream()
                .limit(chaptersToDeleteGuesstimate)
                .forEach(this::deleteChapter);
            return chaptersToDeleteGuesstimate;
        } catch (IOException e) {
            throw new RuntimeException("Cannot list chapters in cache directory!", e);
        }
    }

    private DataSize getCacheSize() {
        File cacheRoot = cacheDirectoryPath.toFile();
        long totalSpaceBytes = cacheRoot.getTotalSpace();
        long usableSpaceBytes = cacheRoot.getUsableSpace();
        long usedSpaceBytes = totalSpaceBytes - usableSpaceBytes;
        return DataSize.ofBytes(usedSpaceBytes);
    }

    private double getFillPercentage() {
        long currentCacheSizeBytes = getCacheSize().toBytes();
        long maxCacheSizeBytes = DataSize.ofGigabytes(cacheSettings.getSizeGib()).toBytes();

        return ((double) currentCacheSizeBytes / (double) maxCacheSizeBytes) * 100.0;
    }

    private void deleteChapter(Path chapter) {
        try {
            Files.walkFileTree(chapter, chapterDeleter);
        } catch (IOException e) {
            LOGGER.error("Could not delete chapter at {}", chapter, e);
        }
    }

}
