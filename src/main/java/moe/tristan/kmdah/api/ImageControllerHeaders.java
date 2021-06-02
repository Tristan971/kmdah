package moe.tristan.kmdah.api;

import java.time.Duration;
import java.util.List;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.service.gossip.InstanceId;
import moe.tristan.kmdah.service.images.ImageContent;

@Component
public class ImageControllerHeaders {

    private static final List<String> EXPOSE_HEADERS = List.of("*");
    private static final CacheControl CACHE_CONTROL = CacheControl
        .maxAge(Duration.ofSeconds(1209600))
        .cachePublic();

    private final InstanceId instanceId;

    public ImageControllerHeaders(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public HttpHeaders buildHeaders(ImageContent imageContent) {
        HttpHeaders headers = new HttpHeaders();

        // MDAH spec headers
        headers.setAccessControlAllowOrigin("*");
        headers.setAccessControlExposeHeaders(EXPOSE_HEADERS);
        headers.setCacheControl(CACHE_CONTROL);
        headers.add("Timing-Allow-Origin", "*");
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Cache", imageContent.cacheMode().name());
        headers.setLastModified(imageContent.lastModified());

        // match for expected headers
        headers.setContentType(imageContent.contentType());

        // match attempt for optional headers
        imageContent.contentLength().ifPresent(headers::setContentLength);

        // extra kmdah-specific headers
        headers.add("X-Instance-Id", instanceId.id());

        return headers;
    }

}
