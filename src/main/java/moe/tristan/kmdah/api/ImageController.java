package moe.tristan.kmdah.api;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.model.ImageSpec;
import moe.tristan.kmdah.service.images.ImageService;

@RestController
public class ImageController {

    private final ImageService imageService;
    private final ImageRequestTokenValidator tokenValidator;
    private final ImageRequestReferrerValidator referrerValidator;

    public ImageController(ImageService imageService, ImageRequestTokenValidator tokenValidator, ImageRequestReferrerValidator referrerValidator) {
        this.imageService = imageService;
        this.tokenValidator = tokenValidator;
        this.referrerValidator = referrerValidator;
    }

    @GetMapping("/{token}/{image-mode}/{chapterHash}/{fileName}")
    public Flux<DataBuffer> tokenizedImage(
        @PathVariable String token,
        @PathVariable("image-mode") String imageMode,
        @PathVariable String chapterHash,
        @PathVariable String fileName,
        ServerHttpRequest request,
        ServerHttpResponse response
    ) {
        tokenValidator.validate(token, chapterHash);
        return image(imageMode, chapterHash, fileName, request, response);
    }

    @GetMapping("/{image-mode}/{chapterHash}/{fileName}")
    public Flux<DataBuffer> image(
        @PathVariable("image-mode") String imageMode,
        @PathVariable String chapterHash,
        @PathVariable String fileName,
        ServerHttpRequest request,
        ServerHttpResponse response
    ) {
        return serve(imageMode, chapterHash, fileName, request, response);
    }

    private Flux<DataBuffer> serve(String imageMode, String chapterHash, String fileName, ServerHttpRequest request, ServerHttpResponse response) {
        referrerValidator.validate(request.getHeaders().getFirst(HttpHeaders.REFERER));

        ImageSpec imageRequest = new ImageSpec(ImageMode.fromPathFragment(imageMode), chapterHash, fileName);

        return imageService
            .findOrFetch(imageRequest)
            .flatMapMany(image -> {
                // MDAH spec headers
                response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://mangadex.org");
                response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
                response.getHeaders().add(HttpHeaders.CACHE_CONTROL, "public/ max-age=1209600");
                response.getHeaders().add("Timing-Allow-Origin", "https://mangadex.org");
                response.getHeaders().add("X-Content-Type-Options", "nosniff");

                // match for expected headers
                response.getHeaders().setContentType(image.contentType());

                // match attempt for optional headers
                image.contentLength().ifPresent(response.getHeaders()::setContentLength);

                // extra kmdah-specific headers
                response.getHeaders().add("X-Cache-Mode", image.cacheMode().name());

                return image.bytes();
            });
    }

}
