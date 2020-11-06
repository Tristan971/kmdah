package moe.tristan.kmdah.worker.service.images;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.common.mangadex.image.UpstreamImage;
import moe.tristan.kmdah.worker.model.ImageRequest;

@Service
public class ImageFilesystemCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageFilesystemCacheService.class);

    public InputStream openStream(ImageRequest imageRequest) throws IOException {
        Path expectedPath = Paths.get(getImagePath(imageRequest));
        LOGGER.debug("Serving {} from {}", imageRequest, expectedPath);
        return Files.newInputStream(expectedPath);
    }

    public CompletionStage<UpstreamImage> writeAsync(ImageRequest imageRequest, UpstreamImage image) {
        return CompletableFuture.supplyAsync(() -> writeImageSync(imageRequest, image));
    }

    private UpstreamImage writeImageSync(ImageRequest imageRequest, UpstreamImage upstreamImage) {
        Path savePath = Paths.get(getImagePath(imageRequest));
        try {
            Files.createDirectories(savePath.getParent());
            Files.write(savePath, upstreamImage.getBytes(), StandardOpenOption.CREATE);
            return upstreamImage;
        } catch (IOException e) {
            throw new RuntimeException("Could not persist upstream image for " + imageRequest + " at path " + savePath);
        }
    }

    private String getImagePath(ImageRequest imageRequest) {
        return String.join(File.separator, imageRequest.getChapter(), imageRequest.getMode().name(), imageRequest.getFile());
    }

}
