package moe.tristan.kmdah.api;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageService;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.validation.ImageRequestReferrerValidator;
import moe.tristan.kmdah.service.images.validation.ImageRequestTokenValidator;
import moe.tristan.kmdah.service.metrics.ImageMetrics;

@RestController
public class ImageController {

    private final ImageService imageService;
    private final ImageMetrics imageMetrics;
    private final ImageControllerHeaders controllerHeaders;
    private final ImageRequestTokenValidator tokenValidator;
    private final ImageRequestReferrerValidator referrerValidator;

    public ImageController(
        ImageService imageService,
        ImageMetrics imageMetrics,
        ImageRequestTokenValidator tokenValidator,
        ImageControllerHeaders controllerHeaders,
        ImageRequestReferrerValidator referrerValidator
    ) {
        this.imageService = imageService;
        this.imageMetrics = imageMetrics;
        this.tokenValidator = tokenValidator;
        this.controllerHeaders = controllerHeaders;
        this.referrerValidator = referrerValidator;
    }

    @GetMapping("/{token}/{image-mode}/{chapterHash}/{fileName}")
    public ResponseEntity<Resource> tokenizedImage(
        @PathVariable String token,
        @PathVariable("image-mode") String imageMode,
        @PathVariable String chapterHash,
        @PathVariable String fileName,
        HttpServletRequest request
    ) {
        tokenValidator.validate(token, chapterHash);
        return image(imageMode, chapterHash, fileName, request);
    }

    @GetMapping("/{image-mode}/{chapterHash}/{fileName}")
    public ResponseEntity<Resource> image(
        @PathVariable("image-mode") String imageMode,
        @PathVariable String chapterHash,
        @PathVariable String fileName,
        HttpServletRequest request
    ) {
        return serve(imageMode, chapterHash, fileName, request);
    }

    private ResponseEntity<Resource> serve(String imageMode, String chapterHash, String fileName, HttpServletRequest request) {
        long startServe = System.nanoTime();

        if (request.getHeader(HttpHeaders.REFERER) != null) {
            referrerValidator.validate(request.getHeaders(HttpHeaders.REFERER).nextElement());
        }

        if (request.getHeader(HttpHeaders.IF_MODIFIED_SINCE) != null) {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }

        ImageSpec imageRequest = new ImageSpec(ImageMode.fromPathFragment(imageMode), chapterHash, fileName);

        ImageContent imageContent = imageService.findOrFetch(imageRequest);
        HttpHeaders headers = controllerHeaders.buildHeaders(imageContent);

        imageMetrics.recordServe(startServe, imageContent.cacheMode());

        return new ResponseEntity<>(
            imageContent.resource(),
            headers,
            HttpStatus.OK
        );
    }

}
