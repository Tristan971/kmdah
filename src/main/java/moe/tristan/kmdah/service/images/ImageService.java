package moe.tristan.kmdah.service.images;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.cache.ImageCache;
import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;
import moe.tristan.kmdah.service.metrics.CacheModeCounter;
import reactor.core.publisher.Mono;

@Service
public class ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

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
            .switchIfEmpty(mangadexImageService.download(imageSpec));
    }

}
