package moe.tristan.kmdah.service.images;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.service.gossip.messages.LeaderImageServerEvent;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.metrics.ImageMetrics;
import moe.tristan.kmdah.util.ContentCallbackInputStream;

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
        imageMetrics.recordSearchFromCache(startSearch, cacheLookup.isPresent());

        ImageContent imageContent = cacheLookup.orElseGet(() -> {
            long startUptreamFetch = System.nanoTime();
            ImageContent upstreamResponseContent = fetchFromUpstream(imageSpec);
            imageMetrics.recordSearchFromUpstream(startUptreamFetch);
            return upstreamResponseContent;
        });

        LOGGER.info("Cache {} for {}", imageContent.cacheMode(), imageSpec);
        imageMetrics.recordSearch(startSearch, imageContent.cacheMode());

        return imageContent;
    }

    private ImageContent fetchFromUpstream(ImageSpec imageSpec) {
        ImageContent upstreamContent = mangadexImageService.download(imageSpec, upstreamServerUri);

        try {
            Consumer<byte[]> cacheSaveCallback = bytes -> {
                LOGGER.info("Content of {} fully read from upstream. Triggering cache saving.", imageSpec);
                cachedImageService.saveImage(imageSpec, new ByteArrayInputStream(bytes));
            };

            return new ImageContent(
                new InputStreamResource(new ContentCallbackInputStream(upstreamContent.resource().getInputStream(), cacheSaveCallback)),
                upstreamContent.contentType(),
                upstreamContent.contentLength(),
                upstreamContent.lastModified(),
                upstreamContent.cacheMode()
            );
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open upstream response for reading!", e);
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
