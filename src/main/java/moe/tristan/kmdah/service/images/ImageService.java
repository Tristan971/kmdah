package moe.tristan.kmdah.service.images;

import static reactor.core.publisher.Mono.defer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import moe.tristan.kmdah.cache.CacheMode;
import moe.tristan.kmdah.cache.CachedImageService;
import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;
import moe.tristan.kmdah.service.metrics.CacheModeCounter;

@Service
public class ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);
    private static final Executor CACHE_EXECUTOR = Executors.newWorkStealingPool();

    private final CachedImageService cachedImageService;
    private final CacheModeCounter cacheModeCounter;
    private final MangadexImageService mangadexImageService;

    public ImageService(
        CachedImageService cachedImageService,
        CacheModeCounter cacheModeCounter,
        MangadexImageService mangadexImageService
    ) {
        this.cachedImageService = cachedImageService;
        this.cacheModeCounter = cacheModeCounter;
        this.mangadexImageService = mangadexImageService;
    }

    public Mono<ImageContent> findOrFetch(ImageSpec imageSpec) {
        return fetchFromCache(imageSpec)
            .onErrorResume(error -> {
                LOGGER.error("Failed pulling {} from cache!", imageSpec, error);
                return defer(() -> fetchFromUpstream(imageSpec));
            })
            .switchIfEmpty(defer(() -> fetchFromUpstream(imageSpec)));
    }

    private Mono<ImageContent> fetchFromCache(ImageSpec imageSpec) {
        return cachedImageService
            .findImage(imageSpec)
            .doOnNext(content -> {
                LOGGER.info("Cache hit for {} - {}", imageSpec, content);
                cacheModeCounter.record(CacheMode.HIT);
            });
    }

    private Mono<ImageContent> fetchFromUpstream(ImageSpec imageSpec) {
        return mangadexImageService
            .download(imageSpec, "https://s2.mangadex.org")
            .doFirst(() -> {
                LOGGER.info("Cache miss for {}", imageSpec);
                cacheModeCounter.record(CacheMode.MISS);
            })
            .doOnNext(content -> {
                LOGGER.info("Scheduling caching for {}", imageSpec);
                CACHE_EXECUTOR.execute(() ->
                    cachedImageService
                        .saveImage(imageSpec, content)
                        .doOnSuccess(res -> LOGGER.info("Done caching {}", imageSpec))
                        .subscribe()
                );
            });
    }

}
