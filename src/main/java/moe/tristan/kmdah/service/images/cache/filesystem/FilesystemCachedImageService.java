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
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.util.unit.DataSize;

import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;

public class FilesystemCachedImageService implements CachedImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemCachedImageService.class);

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
    public void saveImage(ImageSpec imageSpec, ImageContent imageContent) {
        Path finalFile = specToPath(imageSpec);
        Path tmpFile = finalFile.resolveSibling(imageSpec.file() + ".tmp");

        if (Files.exists(finalFile)) {
            LOGGER.warn("Final file already exists at {} for image {}. Dropping upstream response", finalFile, imageSpec);
            drainContent(imageContent.resource());
        }

        if (Files.exists(tmpFile)) {
            boolean isStale = isTmpFileStale(tmpFile);
            if (isStale) {
                try {
                    Files.delete(tmpFile);
                } catch (IOException e) {
                    LOGGER.error("Cannot delete stale temporary file {}", tmpFile);
                    drainContent(imageContent.resource());
                    return;
                }
            } else {
                LOGGER.warn("Temporary file exists and isn't stale at {} for image {}. Dropping upstream response", tmpFile, imageSpec);
                drainContent(imageContent.resource());
                return;
            }
        }

        try {
            Files.createDirectories(finalFile.getParent());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create intermediate directories to " + finalFile, e);
        }

        try (
            FileChannel tmpFileChannel = FileChannel.open(tmpFile, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            OutputStream tmpFileOutputStream = Channels.newOutputStream(tmpFileChannel)
        ) {
            StreamUtils.copy(
                imageContent.resource().getInputStream(),
                tmpFileOutputStream
            );
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't write upstream content to " + tmpFile + "!", e);
        }

        try {
            Files.move(tmpFile, finalFile);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't commit temporary file " + tmpFile + " to final location " + finalFile, e);
        }
    }

    private boolean isTmpFileStale(Path tmpFile) {
        Instant now = Instant.now();
        Instant deadFileCutoff = now.minus(10, ChronoUnit.MINUTES);
        try {
            Instant tmpFileLastModified = Files.getLastModifiedTime(tmpFile).toInstant();
            if (tmpFileLastModified.isBefore(deadFileCutoff)) {
                Duration tmpFileAge = Duration.between(tmpFileLastModified, now);
                LOGGER.warn(
                    "Temporary file at {} is {} minutes old. Considering stale.",
                    tmpFile,
                    tmpFileAge.get(ChronoUnit.MINUTES)
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

    private void drainContent(Resource resource) {
        try (InputStream stream = resource.getInputStream()) {
            StreamUtils.drain(stream);
        } catch (IOException e) {
            LOGGER.error("Exception while draining inputstream!", e);
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
