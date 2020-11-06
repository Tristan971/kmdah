package moe.tristan.kmdah.worker.api;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import moe.tristan.kmdah.common.mangadex.image.Image;
import moe.tristan.kmdah.common.mangadex.image.ImageMode;
import moe.tristan.kmdah.worker.model.ImageRequest;
import moe.tristan.kmdah.worker.service.images.CachedImageService;

@RestController
public class ImageController {

    private final CachedImageService imageService;

    public ImageController(CachedImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/{token}/{image-mode}/{chapterHash}/{fileName}")
    public InputStreamResource tokenizedImage(
        @PathVariable String token,
        @PathVariable("image-mode") String imageMode,
        @PathVariable String chapterHash,
        @PathVariable String fileName,
        HttpServletResponse response
    ) {
        return serve(response, imageMode, chapterHash, fileName);
    }

    @GetMapping("/{image-mode}/{chapterHash}/{fileName}")
    public InputStreamResource unTokenizedImage(
        @PathVariable("image-mode") String imageMode,
        @PathVariable String chapterHash,
        @PathVariable String fileName,
        HttpServletResponse response
    ) {
        return serve(response, imageMode, chapterHash, fileName);
    }

    private InputStreamResource serve(HttpServletResponse response, String imageMode, String chapter, String file) {
        ImageRequest imageRequest = ImageRequest.of(
            ImageMode.fromPathFragment(imageMode),
            chapter,
            file
        );
        Image image = imageService.findOrFetch(imageRequest);
        response.setContentLength(image.getSize());
        response.setContentType(image.getContentType());

        // MDAH spec headers
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://mangadex.org");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "public/ max-age=1209600");
        response.setHeader("Timing-Allow-Origin", "https://mangadex.org");
        response.setHeader("X-Content-Type-Options", "nosniff");

        return new InputStreamResource(image.getInputStream());
    }

}
