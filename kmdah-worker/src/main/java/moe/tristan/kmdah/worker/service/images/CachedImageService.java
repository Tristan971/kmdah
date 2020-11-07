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

import moe.tristan.kmdah.common.model.ImageContent;
import moe.tristan.kmdah.common.model.persistence.CachedImage;
import moe.tristan.kmdah.common.model.persistence.ImageEntity;
import moe.tristan.kmdah.common.model.persistence.UpstreamImage;
import moe.tristan.kmdah.worker.model.ImageRequest;
import moe.tristan.kmdah.worker.service.mangadex.MangadexImageService;

@Service
public class CachedImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedImageService.class);

    private final MangadexImageService mangadexImageService;
    private final ImagesRepository imagesRepository;
    private final ImageFilesystemCacheService filesystemCacheService;

    public CachedImageService(
        MangadexImageService mangadexImageService,
        ImagesRepository imagesRepository,
        ImageFilesystemCacheService filesystemCacheService
    ) {
        this.mangadexImageService = mangadexImageService;
        this.imagesRepository = imagesRepository;
        this.filesystemCacheService = filesystemCacheService;
    }

    public ImageContent findOrFetch(ImageRequest imageRequest) {
        Optional<ImageEntity> cachedImageSearch = imagesRepository.findByFilenameAndChapterHashAndMode(
            imageRequest.getFilename(),
            imageRequest.getChapterHash(),
            imageRequest.getMode()
        );

        // if present in db, try returning from filesystem -- if this fails, use upstream instead
        if (cachedImageSearch.isPresent()) {
            ImageEntity cachedImage = cachedImageSearch.get();
            try {
                return loadFromFilecache(imageRequest, cachedImage);
            } catch (IOException cause) {
                LOGGER.error("Image {} was marked as present in cache but couldn't be loaded! Retrying from upstream.", imageRequest, cause);
            }
        }

        // if we it wasn't in cache, or could not be loaded from it, use upstream
        UpstreamImage upstreamImage = loadFromUpstream(imageRequest);

        // schedule disk saving, and db persistence once that has successfully finished
        // db persistence uses its own threadpool, to allow it to have more or less pressure if needed
        filesystemCacheService
            .writeAsync(imageRequest, upstreamImage)
            .thenAcceptAsync(savedImage -> persistSavedImage(imageRequest, savedImage));

        return upstreamImage;
    }

    private CachedImage loadFromFilecache(ImageRequest imageRequest, ImageEntity imageEntity) throws IOException {
        InputStream cachedImageStream = filesystemCacheService.openStream(imageRequest);
        return CachedImage
            .builder()
            .contentType(imageEntity.getContentType())
            .size(imageEntity.getSize())
            .inputStream(cachedImageStream)
            .build();
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
            .size(bytes.length)
            .build();
    }

    private void persistSavedImage(ImageRequest request, UpstreamImage image) {
        ImageEntity imageEntity = new ImageEntity(request.getFilename(), request.getChapterHash(), request.getMode(), image.getContentType(), image.getBytes().length);
        imagesRepository.save(imageEntity);
        LOGGER.info("Persisted image {} to cache database", imageEntity);
    }

}
