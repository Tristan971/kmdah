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

        long startServe = System.nanoTime();

        return imageService
            .findOrFetch(imageRequest)
            .doOnNext(content -> controllerHeaders.addHeaders(response.getHeaders(), content))
            .flatMapMany(content -> {
                long startLoad = System.nanoTime();
                return content
                    .bytes()
                    .doOnComplete(() -> imageMetrics.recordLoad(startLoad, content.cacheMode()))
                    .doOnSubscribe(__ -> imageMetrics.recordServe(startServe, content.cacheMode()));
            });
    }

}
