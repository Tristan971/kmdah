package moe.tristan.kmdah.operator.service.vacuum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.unit.DataSize;

import moe.tristan.kmdah.common.model.configuration.CacheSettings;
import moe.tristan.kmdah.common.model.persistence.ImageEntity;
import moe.tristan.kmdah.operator.model.vacuum.VacuumResult;

import io.micrometer.core.annotation.Timed;

/**
 * Deletes stuff from the cache, 100-by-100, until we're under the cache size
 */
@Service
public class VacuumService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VacuumService.class);

    private final DataSource dataSource;
    private final CacheSettings cacheSettings;
    private final ImageRepository imageRepository;

    public VacuumService(
        DataSource dataSource,
        CacheSettings cacheSettings,
        ImageRepository imageRepository
    ) {
        this.dataSource = dataSource;
        this.cacheSettings = cacheSettings;
        this.imageRepository = imageRepository;
    }

    @Timed
    protected void vacuumUntilUnderCacheMaxSize() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        DataSize currentCacheSize = DataSize.ofBytes(estimateCacheSize());
        DataSize maxCacheSize = DataSize.ofGigabytes(cacheSettings.getMaxSizeGibibytes());
        LOGGER.info("Cache size uage: {}/{} GB", currentCacheSize.toGigabytes(), maxCacheSize.toGigabytes());

        DataSize excess = DataSize.ofBytes(currentCacheSize.toBytes() - maxCacheSize.toBytes());

        if (excess.toGigabytes() <= 0) {
            LOGGER.info("No need for cache vacuuming ({}GB under requested size)", Math.abs(excess.toGigabytes()));
            return;
        }

        long deletedFiles = 0;
        while (excess.toGigabytes() > 0) {
            LOGGER.info("Shrinking cache... (excess: {}GB)", excess.toGigabytes());
            VacuumResult vacuum = vacuum();

            excess = DataSize.ofBytes(excess.toBytes() - vacuum.getFreed());
            deletedFiles += vacuum.getCount();
        }

        stopWatch.stop();
        LOGGER.info("Done shrinking cache - evicted {} files in {}s", deletedFiles, stopWatch.getTotalTimeSeconds());
    }

    @Timed
    protected VacuumResult vacuum() {
        List<ImageEntity> toDelete = imageRepository.findTop100By();

        int count = toDelete.size();
        int freed = toDelete.stream().mapToInt(ImageEntity::getSize).sum();
        VacuumResult vacuumResult = VacuumResult.of(count, freed);

        List<ImageEntity> deletedFromFilesystem = toDelete.stream().filter(deletedImage -> {
            String deletedFilePath = deletedImage.getPath();
            try {
                deleteFromFilesystem(deletedFilePath);
                return true;
            } catch (IOException e) {
                LOGGER.error("Failed to delete {} at path {}", deletedImage, deletedFilePath, e);
                return false;
            }
        }).collect(Collectors.toList());
        imageRepository.deleteInBatch(deletedFromFilesystem);

        return vacuumResult;
    }

    @Timed
    protected long estimateCacheSize() {
        try {
            PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("select sum (size) as total from mdah.images");
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getLong("total");
        } catch (SQLException throwables) {
            LOGGER.error("Cannot estimate cache size!", throwables);
            return 0;
        }
    }

    private void deleteFromFilesystem(String path) throws IOException {
        Files.deleteIfExists(Paths.get(path));
    }

}
