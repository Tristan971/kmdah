package moe.tristan.kmdah.service.images.cache.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.unit.DataSize;

import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;
import moe.tristan.kmdah.service.images.cache.VacuumingResult.VacuumGranularity;
import moe.tristan.kmdah.util.ThrottledExecutorService;

public class FilesystemCachedImageService implements CachedImageService, HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemCachedImageService.class);

    private static final int NB_CORES = Runtime.getRuntime().availableProcessors();

    // pool of as many threads as CPU cores, with the same amount queued
    private static final ExecutorService WRITE_EXECUTOR_SERVICE = ThrottledExecutorService.from(NB_CORES, NB_CORES, NB_CORES);

    private final FilesystemSettings filesystemSettings;

    public FilesystemCachedImageService(FilesystemSettings filesystemSettings) {
        this.filesystemSettings = filesystemSettings;
        LOGGER.info("Initializing in filesystem mode with {}", filesystemSettings);
        validateDirHealth(filesystemSettings.rootDir());
        LOGGER.info("Successfully validated rootDir {} for usage as cache filesystem!", filesystemSettings.rootDir());
    }

    void validateDirHealth(Path dir) {
        if (dir == null || dir.toString().isBlank()) {
            throw new IllegalStateException("Unset cache directory!");
        }

        if (!dir.isAbsolute()) {
            throw new IllegalStateException(dir + " should be an absolute path!");
        }

        if (!Files.exists(dir)) {
            throw new IllegalStateException(dir + " doesn't exist!");
        }

        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException(dir + " is not a directory!");
        }

        if (filesystemSettings.readOnly()) {
            LOGGER.info("Not executing write access checks for filesystem directory as it is mounted read-only!");
            return;
        }

        String witness = UUID.randomUUID().toString();
        Path testFile = dir.resolve(witness);

        try {
            Files.writeString(testFile, witness, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new IllegalStateException(dir + " cannot be written to!", e);
        }

        try {
            String read = Files.readString(testFile);
            if (!witness.equals(read)) {
                throw new IllegalStateException("Test file had different content on read than on write! Something's wrong!");
            }
        } catch (IOException e) {
            throw new IllegalStateException(dir + " cannot be read from!", e);
        }

        try {
            Files.delete(testFile);
        } catch (IOException e) {
            throw new IllegalStateException(dir + " cannot be deleted from!", e);
        }
    }

    @Override
    public Optional<ImageContent> findImage(ImageSpec imageSpec) {
        Path file = specToPath(filesystemSettings.rootDir(), imageSpec);
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        try {
            long length = Files.size(file);
            Instant lastModified = Files.getLastModifiedTime(file).toInstant();
            MediaType mediaType = MediaType.parseMediaType(Files.probeContentType(file));

            return Optional.of(new ImageContent(
                new FileSystemResource(file),
                mediaType,
                OptionalLong.of(length),
                lastModified,
                CacheMode.HIT
            ));
        } catch (IOException e) {
            LOGGER.error("Cannot read file " + file.toAbsolutePath() + " for image " + imageSpec, e);
            return Optional.empty();
        }
    }

    @Override
    public void saveImage(ImageSpec imageSpec, MediaType mediaType, InputStream inputStream) {
        if (filesystemSettings.readOnly()) {
            LOGGER.warn("Refusing to save image {} when filesystem is set to read-only!", imageSpec);
            return;
        }

        try {
            WRITE_EXECUTOR_SERVICE.submit(() -> {
                try {
                    doSaveImage(imageSpec, inputStream);
                } catch (Exception e) {
                    LOGGER.error("Error during cache saving of {}", imageSpec, e);
                }
            });
        } catch (RejectedExecutionException e) {
            LOGGER.error("Couldn't schedule cache save of {} due to having a full queue of files to commit already.", imageSpec);
        }
    }

    @Override
    public void deleteChapter(ImageSpec imageSpec) {
        if (filesystemSettings.readOnly()) {
            throw new IllegalArgumentException("Refusing to delete chapter when filesystem backend is set to read-only mode.");
        }

        Path path = specToPath(filesystemSettings.rootDir(), imageSpec).getParent();
        try {
            FileUtils.deleteDirectory(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Cannot delete " + imageSpec, e);
        }
    }

    private void doSaveImage(ImageSpec imageSpec, InputStream content) throws IOException {
        Path finalFile = specToPath(filesystemSettings.rootDir(), imageSpec);
        Path tmpFile = finalFile.resolveSibling(imageSpec.file() + ".tmp");

        if (Files.exists(finalFile)) {
            LOGGER.warn("Final file already exists at {} for image {}. Not committing response.", finalFile, imageSpec);
            return;
        }

        if (Files.exists(tmpFile)) {
            if (isTmpFileStale(tmpFile)) {
                Files.delete(tmpFile);
            } else {
                LOGGER.warn("Temporary file exists and isn't stale at {} for image {}. Dropping upstream response", tmpFile, imageSpec);
                return;
            }
        }

        Files.createDirectories(finalFile.getParent());

        try (
            FileChannel tmpFileChannel = FileChannel.open(tmpFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            OutputStream tmpFileOutputStream = Channels.newOutputStream(tmpFileChannel)
        ) {
            content.transferTo(tmpFileOutputStream);
            Files.move(tmpFile, finalFile);
            LOGGER.info("Committed {} to cache", imageSpec);
        } catch (IOException e) {
            LOGGER.error("Couldn't commit {} to cache.", tmpFile, e);
        }
    }

    private boolean isTmpFileStale(Path tmpFile) {
        Instant now = Instant.now();
        Instant deadFileCutoff = now.minusSeconds(10 * 60);
        try {
            Instant tmpFileLastModified = Files.getLastModifiedTime(tmpFile).toInstant();
            if (tmpFileLastModified.isBefore(deadFileCutoff)) {
                Duration tmpFileAge = Duration.between(tmpFileLastModified, now);
                LOGGER.warn(
                    "Temporary file at {} is {} minutes old. Considering stale.",
                    tmpFile,
                    tmpFileAge.toSeconds() / 60
                );
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            LOGGER.error("Cannot determine whether temporary file is stale. Assume it isn't.", e);
            return false;
        }
    }

    @Override
    public VacuumingResult vacuum(VacuumingRequest vacuumingRequest) {
        if (filesystemSettings.readOnly()) {
            LOGGER.info("Not running vacuum over read-only filesystem.");
            return new VacuumingResult(0, DataSize.ofBytes(0L), VacuumGranularity.CHAPTER);
        }

        DataSize originalSpaceUse = getSpaceUsed();
        DataSize targetSpaceUse = vacuumingRequest.targetSize();

        double loadFactor = (double) originalSpaceUse.toBytes() / (double) targetSpaceUse.toBytes();
        LOGGER.info("Cache fill factor: {}% ({}/{}GB)", (int) (loadFactor * 100), originalSpaceUse.toGigabytes(), targetSpaceUse.toGigabytes());

        if (loadFactor < 1.) {
            LOGGER.info("No need for vacuuming");
            return new VacuumingResult(0L, DataSize.ofBytes(0L), VacuumGranularity.CHAPTER);
        } else {
            LOGGER.info("Vacuuming required");
            long deletedChapters = vacuumFilesystem(loadFactor);

            DataSize postCleanupSpaceUse = getSpaceUsed();
            DataSize freedSpace = DataSize.ofBytes(originalSpaceUse.toBytes() - postCleanupSpaceUse.toBytes());

            return new VacuumingResult(deletedChapters, freedSpace, VacuumGranularity.CHAPTER);
        }

    }

    private Path specToPath(Path dir, ImageSpec spec) {
        return dir
            .resolve(spec.mode().getPathFragment())
            .resolve(spec.chapter())
            .resolve(spec.file());
    }

    @Override
    public Health health() {
        try {
            validateDirHealth(filesystemSettings.rootDir());
            return Health.up().build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }

    private long vacuumFilesystem(double loadFactor) {
        Path dataDir = filesystemSettings.rootDir().resolve(ImageMode.DATA.getPathFragment());
        Path dataSaverDir = filesystemSettings.rootDir().resolve(ImageMode.DATA_SAVER.getPathFragment());

        Set<Path> chapters;
        try (
            Stream<Path> dataChapters = Files.list(dataDir);
            Stream<Path> dataSaverChapters = Files.list(dataSaverDir)
        ) {
            chapters = Stream.concat(dataChapters, dataSaverChapters).collect(Collectors.toSet());
            LOGGER.info("Gathered {} chapters", chapters.size());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan directory!", e);
        }


        long chaptersCount = chapters.size();
        long wantToDelete = (long) ((double) chaptersCount * (loadFactor - 1.));

        LOGGER.info("Deleting {}/{} chapters", wantToDelete, chaptersCount);

        chapters.stream().unordered().limit(wantToDelete).forEach(path -> {
            try {
                FileSystemUtils.deleteRecursively(path);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot delete chapter at " + path, e);
            }
        });

        return wantToDelete;
    }

    private DataSize getSpaceUsed() {
        try {
            FileStore fileStore = Files.getFileStore(filesystemSettings.rootDir());
            long usedSpaceBytes = fileStore.getTotalSpace() - fileStore.getUnallocatedSpace();
            return DataSize.ofBytes(usedSpaceBytes);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot get FileStore information", e);
        }
    }

}
