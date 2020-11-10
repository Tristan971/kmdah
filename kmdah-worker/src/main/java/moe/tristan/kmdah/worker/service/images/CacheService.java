package moe.tristan.kmdah.worker.service.images;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import io.micrometer.core.annotation.Timed;
import moe.tristan.kmdah.common.model.persistence.CachedImage;
import moe.tristan.kmdah.common.model.persistence.UpstreamImage;
import moe.tristan.kmdah.common.model.settings.CacheSettings;
import moe.tristan.kmdah.worker.model.ImageRequest;

@Service
public class CacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

    private final AmazonS3 s3CacheClient;
    private final CacheSettings cacheSettings;

    public CacheService(AmazonS3 s3CacheClient, CacheSettings cacheSettings) {
        this.s3CacheClient = s3CacheClient;
        this.cacheSettings = cacheSettings;
    }

    @Timed
    public Optional<CachedImage> findCachedImage(ImageRequest imageRequest) throws IOException {
        String expectedPath = imageRequest.getPath();
        LOGGER.debug("Serving {} from {}", imageRequest, expectedPath);

        if (s3CacheClient.doesObjectExist(cacheSettings.getBucketName(), expectedPath)) {
            S3Object object = s3CacheClient.getObject(cacheSettings.getBucketName(), expectedPath);
            ObjectMetadata objectMetadata = object.getObjectMetadata();

            long contentLength = objectMetadata.getContentLength();
            Optional<String> contentType = Optional
                .of(objectMetadata.getContentType())
                .filter(ctype -> !Mimetypes.MIMETYPE_OCTET_STREAM.equals(ctype));

            CachedImage image = CachedImage
                .builder()
                .inputStream(object.getObjectContent())
                .contentType(contentType)
                .contentLength(contentLength)
                .build();

            return Optional.of(image);
        } else {
            return Optional.empty();
        }
    }

    public void writeAsync(ImageRequest imageRequest, UpstreamImage image) {
        CompletableFuture.runAsync(() -> writeImageSync(imageRequest, image));
    }

    @Timed
    protected void writeImageSync(ImageRequest imageRequest, UpstreamImage upstreamImage) {
        String savePath = imageRequest.getPath();
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(upstreamImage.getBytes().length);
            upstreamImage.getContentType().ifPresent(objectMetadata::setContentType);

            s3CacheClient.putObject(cacheSettings.getBucketName(), savePath, upstreamImage.getInputStream(), objectMetadata);
        } catch (Exception e) {
            throw new RuntimeException("Could not persist upstream image for " + imageRequest + " at path " + savePath, e);
        }
    }

}
