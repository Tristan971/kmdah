package moe.tristan.kmdah.service.images;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.service.gossip.messages.LeaderImageServerEvent;
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
        return mangadexImageService.download(imageSpec, upstreamServerUri);
    }

    @EventListener(LeaderImageServerEvent.class)
    public void onLeaderImageServerEvent(LeaderImageServerEvent leaderImageServerEvent) {
        if (!upstreamServerUri.equals(leaderImageServerEvent.imageServer())) {
            LOGGER.info("Changed upstream server uri to: {}", leaderImageServerEvent.imageServer());
            this.upstreamServerUri = leaderImageServerEvent.imageServer();
        }
    }

}
