package moe.tristan.kmdah.service.images;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Optional;

import org.bouncycastle.util.io.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.service.gossip.messages.LeaderImageServerEvent;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.metrics.ImageMetrics;

@Service
public class ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private final CachedImageService cachedImageService;
    private final MangadexImageService mangadexImageService;
    private final ImageMetrics imageMetrics;

    private String upstreamServerUri = "https://s2.mangadex.org";

    public ImageService(
        CachedImageService cachedImageService,
        MangadexImageService mangadexImageService,
        ImageMetrics imageMetrics
    ) {
        this.cachedImageService = cachedImageService;
        this.mangadexImageService = mangadexImageService;
        this.imageMetrics = imageMetrics;
    }

    public ImageContent findOrFetch(ImageSpec imageSpec) {
        long startSearch = System.nanoTime();

        Optional<ImageContent> cacheLookup;
        try {
            cacheLookup = cachedImageService.findImage(imageSpec);
        } catch (Exception e) {
            LOGGER.error("Failed searching image {} in cache", imageSpec, e);
            cacheLookup = Optional.empty();
        }

        ImageContent imageContent = cacheLookup.orElseGet(() -> fetchFromUpstream(imageSpec));

        LOGGER.info("Cache {} for {}", imageContent.cacheMode(), imageSpec);
        imageMetrics.recordSearch(startSearch, imageContent.cacheMode());

        return imageContent;
    }

    private ImageContent fetchFromUpstream(ImageSpec imageSpec) {
        ImageContent upstreamContent = mangadexImageService.download(imageSpec, upstreamServerUri);

        try {
            InputStream upstreamIs = upstreamContent.resource().getInputStream();

            PipedOutputStream cacheOutputStream = new PipedOutputStream();
            PipedInputStream cacheInputStream = new PipedInputStream(cacheOutputStream);
            TeeInputStream responseInputStream = new TeeInputStream(upstreamIs, cacheOutputStream);

            ImageContent cacheSaveContent = new ImageContent(
                new InputStreamResource(cacheInputStream),
                upstreamContent.contentType(),
                upstreamContent.contentLength(),
                upstreamContent.lastModified(),
                CacheMode.MISS
            );
            cachedImageService.saveImage(imageSpec, cacheSaveContent);

            return new ImageContent(
                new InputStreamResource(responseInputStream),
                upstreamContent.contentType(),
                upstreamContent.contentLength(),
                upstreamContent.lastModified(),
                CacheMode.MISS
            );
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @EventListener(LeaderImageServerEvent.class)
    public void onLeaderImageServerEvent(LeaderImageServerEvent leaderImageServerEvent) {
        if (!upstreamServerUri.equals(leaderImageServerEvent.imageServer())) {
            LOGGER.info("Changed upstream server uri to: {}", leaderImageServerEvent.imageServer());
            this.upstreamServerUri = leaderImageServerEvent.imageServer();
        }
    }

}
