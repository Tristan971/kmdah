package moe.tristan.kmdah.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.service.gossip.InstanceId;
import moe.tristan.kmdah.service.images.ImageContent;

@Component
public class ImageControllerHeaders {

    private final InstanceId instanceId;
    private final String serverHeader;

    public ImageControllerHeaders(InstanceId instanceId, @Value("${spring.application.version}") String version) {
        this.instanceId = instanceId;
        this.serverHeader = "kmdah " + version + " (" + MangadexApi.SPEC_VERSION + ") - github.com/Tristan971/kmdah";
    }

    public void addHeaders(HttpHeaders headers, ImageContent imageContent) {
        // MDAH spec headers
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://mangadex.org");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        headers.add(HttpHeaders.CACHE_CONTROL, "public/ max-age=1209600");
        headers.add("Timing-Allow-Origin", "https://mangadex.org");
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Cache", imageContent.cacheMode().name());

        // match for expected headers
        headers.setContentType(imageContent.contentType());

        // match attempt for optional headers
        imageContent.contentLength().ifPresent(headers::setContentLength);

        // extra kmdah-specific headers
        headers.add(HttpHeaders.SERVER, serverHeader);
        headers.add("X-Instance-Id", instanceId.id());
        headers.add("X-Cache-Mode", imageContent.cacheMode().name());
    }

}
