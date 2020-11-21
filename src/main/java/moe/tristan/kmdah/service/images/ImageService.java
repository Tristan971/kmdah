package moe.tristan.kmdah.service.images;

import static reactor.core.publisher.Mono.defer;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import moe.tristan.kmdah.cache.CacheMode;
import moe.tristan.kmdah.cache.ImageCache;
import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;
import moe.tristan.kmdah.service.metrics.CacheModeCounter;

@Service
public class ImageService {

    private final ImageCache imageCache;
    private final CacheModeCounter cacheModeCounter;
    private final MangadexImageService mangadexImageService;

    public ImageService(
        ImageCache imageCache,
        CacheModeCounter cacheModeCounter,
        MangadexImageService mangadexImageService
    ) {
        this.imageCache = imageCache;
        this.cacheModeCounter = cacheModeCounter;
        this.mangadexImageService = mangadexImageService;
    }

    public Mono<ImageContent> findOrFetch(ImageSpec imageSpec) {
        return fetchFromCache(imageSpec)
            .switchIfEmpty(defer(() -> fetchFromUpstream(imageSpec)));
    }

    private Mono<ImageContent> fetchFromCache(ImageSpec imageSpec) {
        return imageCache
            .findImage(imageSpec)
            .doOnNext(__ -> cacheModeCounter.record(CacheMode.HIT));
    }

    private Mono<ImageContent> fetchFromUpstream(ImageSpec imageSpec) {
        return mangadexImageService
            .download(imageSpec, "https://tbd")
            .doOnNext(content -> imageCache.saveImage(imageSpec, content))
            .doOnNext(__ -> cacheModeCounter.record(CacheMode.MISS));
    }

}
