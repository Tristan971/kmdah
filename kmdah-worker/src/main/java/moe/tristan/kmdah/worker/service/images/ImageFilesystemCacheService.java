package moe.tristan.kmdah.worker.service.images;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.common.model.persistence.UpstreamImage;
import moe.tristan.kmdah.common.model.settings.CacheSettings;
import moe.tristan.kmdah.worker.model.ImageRequest;

@Service
public class ImageFilesystemCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageFilesystemCacheService.class);

    private final CacheSettings cacheSettings;

    public ImageFilesystemCacheService(CacheSettings cacheSettings) {
        this.cacheSettings = cacheSettings;
    }

    public Optional<InputStream> findCachedImage(ImageRequest imageRequest) throws IOException {
        Path expectedPath = getAbsolutePath(imageRequest);
        LOGGER.debug("Serving {} from {}", imageRequest, expectedPath);

        if (Files.exists(expectedPath)) {
            return Optional.of(Files.newInputStream(expectedPath));
        } else {
            return Optional.empty();
        }
    }

    public void writeAsync(ImageRequest imageRequest, UpstreamImage image) {
        CompletableFuture.supplyAsync(() -> writeImageSync(imageRequest, image));
    }

    private UpstreamImage writeImageSync(ImageRequest imageRequest, UpstreamImage upstreamImage) {
        Path savePath = getAbsolutePath(imageRequest);
        try {
            Files.createDirectories(savePath.getParent());
            Files.write(savePath, upstreamImage.getBytes(), StandardOpenOption.CREATE);
            return upstreamImage;
        } catch (IOException e) {
            throw new RuntimeException("Could not persist upstream image for " + imageRequest + " at path " + savePath);
        }
    }

    private Path getAbsolutePath(ImageRequest imageRequest) {
        Path cacheRoot = Paths.get(cacheSettings.getRoot());
        return cacheRoot.resolve(imageRequest.getPath()).toAbsolutePath();
    }

}
