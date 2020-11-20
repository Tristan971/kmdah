package moe.tristan.kmdah.service.images;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.cache.CacheMode;
import moe.tristan.kmdah.cache.ImageCache;
import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;
import moe.tristan.kmdah.service.metrics.CacheModeCounter;

@Service
public class ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private final ImageCache imageCache;
    private final CacheModeCounter cacheModeCounter;
    private final MangadexImageService mangadexImageService;

    public ImageService(
        ImageCache imageCache, CacheModeCounter cacheModeCounter,
        MangadexImageService mangadexImageService
    ) {
        this.imageCache = imageCache;
        this.cacheModeCounter = cacheModeCounter;
        this.mangadexImageService = mangadexImageService;
    }

    public ImageContent findOrFetch(ImageSpec imageSpec) {
        Optional<ImageContent> cachedImageSearch = Optional.empty();
        try {
            cachedImageSearch = imageCache.findImage(imageSpec);
        } catch (Exception e) {
            LOGGER.info("Could not load image from cache!", e);
        }

        if (cachedImageSearch.isPresent()) {
            cacheModeCounter.record(CacheMode.HIT);
            return cachedImageSearch.get();
        }

        // if we it wasn't in cache, or could not be loaded from it, use upstream
        cacheModeCounter.record(CacheMode.MISS);
        ImageContent upstreamImage = loadFromUpstream(imageSpec);

        // schedule disk saving
        imageCache.saveImage(imageSpec, upstreamImage);

        return upstreamImage;
    }

    private ImageContent loadFromUpstream(ImageSpec imageRequest) {
        ResponseEntity<byte[]> upstreamImage = mangadexImageService.download(imageRequest);
        MediaType contentType = upstreamImage.getHeaders().getContentType();
        if (contentType == null) {
            throw new IllegalStateException("Upstream didn't set a content type!");
        }

        byte[] bytes = upstreamImage.getBody();
        return new ImageContent(
            new ByteArrayInputStream(requireNonNull(bytes)),
            contentType,
            bytes.length,
            CacheMode.MISS
        );
    }

}
