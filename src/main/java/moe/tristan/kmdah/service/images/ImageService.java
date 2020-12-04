package moe.tristan.kmdah.service.images;

import static reactor.core.publisher.Mono.defer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Mono<ImageContent> findOrFetch(ImageSpec imageSpec) {
        long startSearch = System.nanoTime();

        return fetchFromCache(imageSpec)
            .onErrorResume(error -> {
                LOGGER.error("Failed pulling {} from cache!", imageSpec, error);
                return defer(() -> fetchFromUpstream(imageSpec));
            })
            .switchIfEmpty(defer(() -> fetchFromUpstream(imageSpec)))
            .doOnSuccess(content -> {
                LOGGER.info("Cache {} for {}", content.cacheMode(), imageSpec);
                imageMetrics.recordSearch(startSearch, content.cacheMode());
            });
    }

    private Mono<ImageContent> fetchFromCache(ImageSpec imageSpec) {
        return cachedImageService.findImage(imageSpec);
    }

    private Mono<ImageContent> fetchFromUpstream(ImageSpec imageSpec) {
        return mangadexImageService
            .download(imageSpec, upstreamServerUri)
            .map(content -> {
                Flux<DataBuffer> multicaster = content
                    .bytes()
                    .map(DataBufferUtils::retain)
                    .publish()
                    .autoConnect(2);

                long startSave = System.nanoTime();
                cachedImageService
                    .saveImage(
                        imageSpec,
                        new ImageContent(
                            multicaster,
                            content.contentType(),
                            content.contentLength(),
                            content.cacheMode()
                        )
                    )
                    .doOnSuccess(__ -> imageMetrics.recordSave(startSave))
                    .subscribe();

                return new ImageContent(
                    multicaster,
                    content.contentType(),
                    content.contentLength(),
                    content.cacheMode()
                );
            });
    }

    @EventListener(LeaderImageServerEvent.class)
    public void onLeaderImageServerEvent(LeaderImageServerEvent leaderImageServerEvent) {
        if (!upstreamServerUri.equals(leaderImageServerEvent.imageServer())) {
            LOGGER.info("Changed upstream server uri to: {}", leaderImageServerEvent.imageServer());
            this.upstreamServerUri = leaderImageServerEvent.imageServer();
        }
    }

}
