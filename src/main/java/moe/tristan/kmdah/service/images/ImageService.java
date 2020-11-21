package moe.tristan.kmdah.service.images;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import moe.tristan.kmdah.cache.ImageCache;
import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;
import moe.tristan.kmdah.service.metrics.CacheModeCounter;

@Service
public class ImageService {

    private final ImageCache imageCache;
    private final MangadexImageService mangadexImageService;

    private final CacheModeCounter cacheModeCounter;

    public ImageService(
        ImageCache imageCache,
        MangadexImageService mangadexImageService,
        CacheModeCounter cacheModeCounter
    ) {
        this.imageCache = imageCache;
        this.mangadexImageService = mangadexImageService;
        this.cacheModeCounter = cacheModeCounter;
    }

    public Mono<ImageContent> findOrFetch(ImageSpec imageSpec) {
        return imageCache
            .findImage(imageSpec)
            .switchIfEmpty(fetchFromUpstream(imageSpec));
    }

    private Mono<ImageContent> fetchFromUpstream(ImageSpec imageSpec) {
        return mangadexImageService
            .download(imageSpec, "https://tbd");
    }

}
