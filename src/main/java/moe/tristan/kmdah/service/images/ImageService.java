package moe.tristan.kmdah.service.images;

import static moe.tristan.kmdah.service.metrics.CacheSearchResult.ABORTED;
import static moe.tristan.kmdah.service.metrics.CacheSearchResult.FOUND;
import static moe.tristan.kmdah.service.metrics.CacheSearchResult.NOT_FOUND;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.service.gossip.messages.LeaderImageServerEvent;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.metrics.CacheSearchResult;
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

        boolean aborted = false;
        Optional<ImageContent> cacheLookup;
        try {
            cacheLookup = CompletableFuture.<Optional<ImageContent>>supplyAsync(() -> {
                try {
                    return cachedImageService.findImage(imageSpec);
                } catch (Exception e) {
                    LOGGER.error("Failed searching image {} in cache", imageSpec, e);
                    return Optional.empty();
                }
            }).get(300, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            LOGGER.error("Aborted cache lookup for {} after 300ms.", imageSpec);
            cacheLookup = Optional.empty();
            aborted = true;
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Uncaught exception during cache lookup of {}", imageSpec, e);
            cacheLookup = Optional.empty();
        }

        CacheSearchResult searchResult = aborted
            ? ABORTED
            : cacheLookup.isPresent() ? FOUND : NOT_FOUND;
        imageMetrics.recordSearchFromCache(startSearch, searchResult);

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
