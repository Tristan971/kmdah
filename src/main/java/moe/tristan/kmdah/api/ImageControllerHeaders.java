package moe.tristan.kmdah.api;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.service.gossip.InstanceId;
import moe.tristan.kmdah.service.images.ImageContent;

@Component
public class ImageControllerHeaders {

    private final InstanceId instanceId;

    public ImageControllerHeaders(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public void addHeaders(HttpServletResponse response, ImageContent imageContent) {
        // MDAH spec headers
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://mangadex.org");
        response.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        response.addHeader(HttpHeaders.CACHE_CONTROL, "public/ max-age=1209600");
        response.addHeader("Timing-Allow-Origin", "https://mangadex.org");
        response.addHeader("X-Content-Type-Options", "nosniff");
        response.addHeader("X-Cache", imageContent.cacheMode().name());
        response.setDateHeader(HttpHeaders.LAST_MODIFIED, imageContent.lastModified().toEpochMilli());

        // match for expected headers
        response.setContentType(imageContent.contentType().toString());

        // match attempt for optional headers
        imageContent.contentLength().ifPresent(len -> response.setContentLength(Math.toIntExact(len)));

        // extra kmdah-specific headers
        response.addHeader("X-Instance-Id", instanceId.id());
    }

}
