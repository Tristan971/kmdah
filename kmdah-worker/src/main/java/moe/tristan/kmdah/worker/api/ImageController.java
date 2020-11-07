package moe.tristan.kmdah.worker.api;

import static org.springframework.http.MediaType.parseMediaType;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import moe.tristan.kmdah.common.model.ImageContent;
import moe.tristan.kmdah.common.model.settings.MetadataSettings;
import moe.tristan.kmdah.common.model.mangadex.image.ImageMode;
import moe.tristan.kmdah.worker.model.ImageRequest;
import moe.tristan.kmdah.worker.service.images.CachedImageService;

@Controller
public class ImageController {

    private final MetadataSettings metadataSettings;
    private final CachedImageService imageService;

    public ImageController(MetadataSettings metadataSettings, CachedImageService imageService) {
        this.metadataSettings = metadataSettings;
        this.imageService = imageService;
    }

    @GetMapping("/{token}/{image-mode}/{chapterHash}/{fileName}")
    public HttpEntity<InputStreamResource> tokenizedImage(
        @PathVariable String token,
        @PathVariable("image-mode") String imageMode,
        @PathVariable String chapterHash,
        @PathVariable String fileName
    ) {
        return serve(imageMode, chapterHash, fileName);
    }

    @GetMapping("/{image-mode}/{chapterHash}/{fileName}")
    public HttpEntity<InputStreamResource> unTokenizedImage(
        @PathVariable("image-mode") String imageMode,
        @PathVariable String chapterHash,
        @PathVariable String fileName
    ) {
        return serve(imageMode, chapterHash, fileName);
    }

    private HttpEntity<InputStreamResource> serve(String imageMode, String chapter, String file) {
        ImageRequest imageRequest = ImageRequest
            .builder()
            .mode(ImageMode.fromPathFragment(imageMode))
            .chapterHash(chapter)
            .filename(file)
            .build();

        ImageContent image = imageService.findOrFetch(imageRequest);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(image.getSize());
        responseHeaders.setContentType(parseMediaType(image.getContentType()));

        // MDAH spec headers
        responseHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://mangadex.org");
        responseHeaders.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        responseHeaders.add(HttpHeaders.CACHE_CONTROL, "public/ max-age=1209600");
        responseHeaders.add("Timing-Allow-Origin", "https://mangadex.org");
        responseHeaders.add("X-Content-Type-Options", "nosniff");

        // Extra kmdah headers
        responseHeaders.add("X-Cache-Mode", image.getCacheMode().name());

        InputStreamResource responseStream = new InputStreamResource(image.getInputStream());
        return new HttpEntity<>(responseStream, responseHeaders);
    }

}
