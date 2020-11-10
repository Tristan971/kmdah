package moe.tristan.kmdah.worker.service.images;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.common.api.CacheMode;
import moe.tristan.kmdah.common.model.ImageContent;
import moe.tristan.kmdah.common.model.persistence.CachedImage;
import moe.tristan.kmdah.common.model.persistence.UpstreamImage;
import moe.tristan.kmdah.worker.metrics.CacheModeCounter;
import moe.tristan.kmdah.worker.model.ImageRequest;
import moe.tristan.kmdah.worker.service.mangadex.MangadexImageService;

@Service
public class ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private final CacheModeCounter cacheModeCounter;
    private final MangadexImageService mangadexImageService;
    private final CacheService filesystemCacheService;

    public ImageService(
            CacheModeCounter cacheModeCounter,
            MangadexImageService mangadexImageService,
            CacheService filesystemCacheService
    ) {
        this.cacheModeCounter = cacheModeCounter;
        this.mangadexImageService = mangadexImageService;
        this.filesystemCacheService = filesystemCacheService;
    }

    public ImageContent findOrFetch(ImageRequest imageRequest) {
        Optional<CachedImage> cachedImageSearch = Optional.empty();
        try {
            cachedImageSearch = filesystemCacheService.findCachedImage(imageRequest);
        } catch (IOException e) {
            LOGGER.info("Could not load image from cache!", e);
        }

        if (cachedImageSearch.isPresent()) {
            cacheModeCounter.record(CacheMode.HIT);
            return cachedImageSearch.get();
        }

        // if we it wasn't in cache, or could not be loaded from it, use upstream
        cacheModeCounter.record(CacheMode.MISS);
        UpstreamImage upstreamImage = loadFromUpstream(imageRequest);

        // schedule disk saving
        filesystemCacheService.writeAsync(imageRequest, upstreamImage);

        return upstreamImage;
    }

    private UpstreamImage loadFromUpstream(ImageRequest imageRequest) {
        ResponseEntity<byte[]> upstreamImage = mangadexImageService.download(imageRequest);
        MediaType contentType = upstreamImage.getHeaders().getContentType();
        if (contentType == null) {
            throw new IllegalStateException("Upstream didn't set a content type!");
        }

        byte[] bytes = upstreamImage.getBody();
        return UpstreamImage
                .builder()
                .contentType(contentType.toString())
                .bytes(requireNonNull(bytes))
                .build();
    }

}
