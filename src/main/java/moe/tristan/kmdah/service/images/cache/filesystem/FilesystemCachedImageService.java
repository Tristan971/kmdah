package moe.tristan.kmdah.service.images.cache.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;

import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;

public class FilesystemCachedImageService implements CachedImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemCachedImageService.class);

    private static final int NB_CORES = Runtime.getRuntime().availableProcessors();

    // pool of at most as many threads as CPUs and additionally queued operations
    private static final ExecutorService WRITE_EXECUTOR_SERVICE = new ThreadPoolExecutor(
        NB_CORES,
        NB_CORES,
        0L,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(NB_CORES)
    );

    private final FilesystemSettings filesystemSettings;

    public FilesystemCachedImageService(FilesystemSettings filesystemSettings) {
        this.filesystemSettings = filesystemSettings;

        LOGGER.info("Initializing in filesystem mode with rootDir: {}", filesystemSettings.rootDir());
        validateRootDirHealth();
        LOGGER.info("Successfully validated rootDir {} for usage as cache filesystem!", filesystemSettings.rootDir());
    }

    void validateRootDirHealth() {
        Path rootDir = filesystemSettings.rootDir();
        if (rootDir == null || rootDir.toString().isBlank()) {
            throw new IllegalStateException("Filesystem mode requires setting kmdah.cache.filesystem.rootDir but none was set!");
        }

        if (!rootDir.isAbsolute()) {
            throw new IllegalStateException("rootDir should be an absolute path!");
        }

        if (!Files.exists(rootDir)) {
            throw new IllegalStateException("rootDir doesn't exist!");
        }

        if (!Files.isDirectory(rootDir)) {
            throw new IllegalStateException("rootDir is not a directory!");
        }

        String witness = UUID.randomUUID().toString();
        Path testFile = rootDir.resolve(witness);

        try {
            Files.writeString(testFile, witness, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new IllegalStateException("rootDir cannot be written to!", e);
        }

        try {
            String read = Files.readString(testFile);
            if (!witness.equals(read)) {
                throw new IllegalStateException("Test file had different content on read than on write! Something's wrong!");
            }
        } catch (IOException e) {
            throw new IllegalStateException("rootDir cannot be read from!", e);
        }

        try {
            Files.delete(testFile);
        } catch (IOException e) {
            throw new IllegalStateException("rootDir cannot be deleted from!", e);
        }
    }

    @Override
    public Optional<ImageContent> findImage(ImageSpec imageSpec) {
        Path file = specToPath(imageSpec);
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
            LOGGER.error("Cannot read file " + file.toAbsolutePath().toString() + " for image " + imageSpec, e);
            return Optional.empty();
        }
    }

    @Override
    public void saveImage(ImageSpec imageSpec, MediaType mediaType, InputStream inputStream) {
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

    private void doSaveImage(ImageSpec imageSpec, InputStream content) throws IOException {
        Path finalFile = specToPath(imageSpec);
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
        LOGGER.info("Vacuuming is not supported on {}", getClass().getSimpleName());
        return new VacuumingResult(0L, DataSize.ofBytes(0L));
    }

    private Path specToPath(ImageSpec spec) {
        return filesystemSettings.rootDir().resolve(
            spec.chapter() + File.separator + spec.mode().name() + File.separator + spec.file()
        );
    }

}
