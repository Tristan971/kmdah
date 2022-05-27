package moe.tristan.kmdah.api;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import moe.tristan.kmdah.mangadex.MangadexSettings;
import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageService;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.validation.ImageRequestReferrerValidator;
import moe.tristan.kmdah.service.images.validation.ImageRequestTokenValidator;
import moe.tristan.kmdah.service.images.validation.InvalidImageRequestTokenException;
import moe.tristan.kmdah.service.metrics.ImageMetrics;

@Validated
@RestController
public class ImageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);
    private static final String RX_MD5 = "^\\w+$";
    private static final String RX_FILENAME = "^[\\w-]+\\.(gif|jpe?g|png)$";

    private static final Set<String> TEST_CHAPTERS = Set.of(
        "1b682e7b24ae7dbdc5064eeeb8e8e353",
        "8172a46adc798f4f4ace6663322a383e"
    );

    private final ImageService imageService;
    private final ImageMetrics imageMetrics;
    private final MangadexSettings mangadexSettings;
    private final ImageControllerHeaders controllerHeaders;
    private final ImageRequestTokenValidator tokenValidator;
    private final ImageRequestReferrerValidator referrerValidator;

    public ImageController(
        ImageService imageService,
        ImageMetrics imageMetrics,
        MangadexSettings mangadexSettings,
        ImageRequestTokenValidator tokenValidator,
        ImageControllerHeaders controllerHeaders,
        ImageRequestReferrerValidator referrerValidator
    ) {
        this.imageService = imageService;
        this.imageMetrics = imageMetrics;
        this.mangadexSettings = mangadexSettings;
        this.tokenValidator = tokenValidator;
        this.controllerHeaders = controllerHeaders;
        this.referrerValidator = referrerValidator;
    }

    @GetMapping("/{token}/{image-mode}/{chapterHash}/{fileName}")
    public ResponseEntity<Resource> tokenizedImage(
        @PathVariable String token,
        @PathVariable("image-mode") String imageMode,
        @Pattern(regexp = RX_MD5) @PathVariable String chapterHash,
        @Pattern(regexp = RX_FILENAME) @PathVariable String fileName,
        HttpServletRequest request
    ) {
        if (mangadexSettings.enforceTokens() && !TEST_CHAPTERS.contains(chapterHash)) {
            tokenValidator.validate(token, chapterHash);
        }
        return serve(imageMode, chapterHash, fileName, request);
    }

    @GetMapping("/{image-mode}/{chapterHash}/{fileName}")
    public ResponseEntity<Resource> image(
        @PathVariable("image-mode") String imageMode,
        @Pattern(regexp = RX_MD5) @PathVariable String chapterHash,
        @Pattern(regexp = RX_FILENAME) @PathVariable String fileName,
        HttpServletRequest request
    ) {
        if (mangadexSettings.enforceTokens() && !TEST_CHAPTERS.contains(chapterHash)) {
            LOGGER.error("Invalid access to untokenized endpoint for {}/{}/{}", imageMode, chapterHash, fileName);
            throw new InvalidImageRequestTokenException("Tokens are currently enforced, only test chapters are allowed.");
        }

        return serve(imageMode, chapterHash, fileName, request);
    }

    @DeleteMapping("/{client-secret}/{image-mode}/{chapterHash}")
    public ResponseEntity<Void> delete(
        @PathVariable("client-secret") String clientSecret,
        @PathVariable("image-mode") String imageMode,
        @Pattern(regexp = RX_MD5) @PathVariable String chapterHash
    ) {
        if (!mangadexSettings.clientSecret().equals(clientSecret)) {
            LOGGER.warn("Attempt to delete image had invalid client secret!");
            return ResponseEntity.status(FORBIDDEN).build();
        }

        ImageSpec imageSpec = new ImageSpec(ImageMode.fromPathFragment(imageMode), chapterHash, "*");
        imageService.delete(imageSpec);
        return ResponseEntity.noContent().build();
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
