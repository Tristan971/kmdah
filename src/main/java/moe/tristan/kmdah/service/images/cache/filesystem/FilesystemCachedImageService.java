package moe.tristan.kmdah.service.images.cache.filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;

public class FilesystemCachedImageService implements CachedImageService, HealthIndicator {

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
    public Mono<ImageContent> findImage(ImageSpec imageSpec) {
        return Mono
            .fromCallable(() -> specToPath(imageSpec))
            .filter(Files::exists)
            .map(file -> {
                try {
                    long length = Files.size(file);
                    Instant lastModified = Files.getLastModifiedTime(file).toInstant();
                    String contentType = Files.probeContentType(file);
                    MediaType mediaType = MediaType.parseMediaType(contentType);

                    return new ImageContent(
                        Flux.defer(() -> DataBufferUtils.read(
                            file,
                            DefaultDataBufferFactory.sharedInstance,
                            DefaultDataBufferFactory.DEFAULT_INITIAL_CAPACITY
                        )),
                        mediaType,
                        OptionalLong.of(length),
                        lastModified,
                        CacheMode.HIT
                    );
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot read file " + file.toAbsolutePath().toString() + " for image " + imageSpec, e);
                }
            });
    }

    @Override
    public Mono<?> saveImage(ImageSpec imageSpec, ImageContent imageContent) {
        return Mono
            .fromCallable(() -> specToPath(imageSpec))
            .flatMap(finalFile -> {
                Path tmpFile = finalFile.resolveSibling(imageSpec.file() + ".tmp");

                Optional<Path> concurrentWriteWitness = Stream.of(finalFile, tmpFile).filter(Files::exists).findFirst();
                if (concurrentWriteWitness.isPresent()) {
                    LOGGER.warn(
                        "File already exists at {} for image {}. Dropping upstream response to avoid concurrent writes.",
                        concurrentWriteWitness.get().toString(),
                        imageSpec
                    );
                    return imageContent.bytes().then();
                }

                return DataBufferUtils.write(
                    imageContent.bytes(),
                    tmpFile,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE
                ).doFirst(() -> {
                    try {
                        Files.createDirectories(finalFile.getParent());
                    } catch (IOException e) {
                        throw new IllegalStateException("Couldn't create directories for file" + finalFile.toString() + "!", e);
                    }
                }).doOnSuccess(__ -> {
                    try {
                        Files.move(
                            tmpFile,
                            finalFile,
                            StandardCopyOption.ATOMIC_MOVE
                        );
                    } catch (IOException e) {
                        LOGGER.error("Failed committing {} to cache as {}", tmpFile, finalFile, e);
                    }
                });
            });
    }

    @Override
    public Mono<VacuumingResult> vacuum(VacuumingRequest vacuumingRequest) {
        LOGGER.info("Vacuuming is not supported on {}", getClass().getSimpleName());
        return Mono.empty();
    }

    private Path specToPath(ImageSpec spec) {
        return filesystemSettings.rootDir().resolve(
            spec.chapter() + File.separator + spec.mode().name() + File.separator + spec.file()
        );
    }

    @Override
    public Health health() {
        try {
            validateRootDirHealth();
            return Health.up().build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }

}
