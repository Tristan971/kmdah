package moe.tristan.kmdah.service.images.cache.filesystem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.OptionalLong;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
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

public class FilesystemCachedImageService implements CachedImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemCachedImageService.class);

    private final FilesystemSettings filesystemSettings;

    public FilesystemCachedImageService(FilesystemSettings filesystemSettings) {
        this.filesystemSettings = filesystemSettings;
    }

    @PostConstruct
    void validateRootDir() {
        Path rootDir = filesystemSettings.rootDir();
        if (rootDir == null || rootDir.toString().isBlank()) {
            throw new IllegalStateException("Filesystem mode requires setting kmdah.cache.filesystem.rootDir but none was set!");
        }

        LOGGER.info("Initializing in filesystem mode with rootDir: {}", rootDir);

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

        LOGGER.info("Successfully validated rootDir {} for usage as cache filesystem!", rootDir);
    }

    @Override
    public Mono<ImageContent> findImage(ImageSpec imageSpec) {
        return Mono
            .just(specToPath(imageSpec))
            .filter(Files::isRegularFile)
            .map(file -> {
                try {
                    long length = Files.size(file);

                    Instant lastModified = Files.getLastModifiedTime(file).toInstant();

                    String contentType = Files.probeContentType(file);
                    MediaType mediaType = MediaType.parseMediaType(contentType);

                    Flux<DataBuffer> bytes = DataBufferUtils.read(
                        file,
                        DefaultDataBufferFactory.sharedInstance,
                        DefaultDataBufferFactory.DEFAULT_INITIAL_CAPACITY
                    );

                    return new ImageContent(
                        bytes,
                        mediaType,
                        OptionalLong.of(length),
                        lastModified,
                        CacheMode.HIT
                    );
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot read file " + file.toAbsolutePath().toString() + " for image " + imageSpec, e);
                }
            })
            .onErrorContinue((err, obj) -> LOGGER.error("Couldn't read {} from cache", imageSpec, err));
    }

    @Override
    public Mono<Path> saveImage(ImageSpec imageSpec, ImageContent imageContent) {
        Path file = specToPath(imageSpec);

        if (Files.exists(file)) {
            LOGGER.warn(
                "File already exists at {} for image {}. Dropping upstream response to avoid concurrent writes.",
                file.toAbsolutePath().toString(),
                imageSpec
            );
            // just subscribe to the stream as a noop and return empty save result
            return imageContent.bytes().then(Mono.just(file));
        }

        return DataBufferUtils.write(
            imageContent.bytes(),
            file,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        ).thenReturn(file);
    }

    @Override
    public Mono<VacuumingResult> vacuum(VacuumingRequest vacuumingRequest) {
        LOGGER.info("Vacuuming is not supported on {}", getClass().getSimpleName());
        return Mono.empty();
    }

    private Path specToPath(ImageSpec spec) {
        return filesystemSettings
            .rootDir()
            .resolve(spec.chapter())
            .resolve(spec.mode().name())
            .resolve(spec.file());
    }

}
