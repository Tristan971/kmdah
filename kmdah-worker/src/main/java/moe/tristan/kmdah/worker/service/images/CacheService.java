package moe.tristan.kmdah.worker.service.images;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import io.micrometer.core.annotation.Timed;
import moe.tristan.kmdah.common.model.persistence.CachedImage;
import moe.tristan.kmdah.common.model.persistence.UpstreamImage;
import moe.tristan.kmdah.common.model.settings.S3Settings;
import moe.tristan.kmdah.worker.model.ImageRequest;

@Service
public class CacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

    private final AmazonS3 s3CacheClient;
    private final S3Settings s3Settings;

    public CacheService(AmazonS3 s3CacheClient, S3Settings s3Settings) {
        this.s3CacheClient = s3CacheClient;
        this.s3Settings = s3Settings;
    }

    @Timed
    public Optional<CachedImage> findCachedImage(ImageRequest imageRequest) {
        String expectedPath = imageRequest.getPath();
        LOGGER.debug("Serving {} from {}", imageRequest, expectedPath);

        try {
            S3Object object = s3CacheClient.getObject(s3Settings.getBucketName(), expectedPath);
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
        } catch (AmazonS3Exception e) {
            LOGGER.info(e.getMessage());
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

            s3CacheClient.putObject(s3Settings.getBucketName(), savePath, upstreamImage.getInputStream(), objectMetadata);
        } catch (Exception e) {
            throw new RuntimeException("Could not persist upstream image for " + imageRequest + " at path " + savePath, e);
        }
    }

}
