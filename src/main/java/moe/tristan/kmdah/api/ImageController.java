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
import moe.tristan.kmdah.mangadex.image.MangadexHeaders;
import moe.tristan.kmdah.model.ImageSpec;
import moe.tristan.kmdah.service.images.ImageService;

@RestController
public class ImageController {

    private final ImageService imageService;
    private final MangadexHeaders mangadexHeaders;
    private final ImageTokenValidator imageTokenValidator;
    private final ImageRequestReferrerValidator imageRequestReferrerValidator;

    public ImageController(
        ImageService imageService,
        MangadexHeaders mangadexHeaders,
        ImageTokenValidator imageTokenValidator,
        ImageRequestReferrerValidator imageRequestReferrerValidator
    ) {
        this.imageService = imageService;
        this.mangadexHeaders = mangadexHeaders;
        this.imageTokenValidator = imageTokenValidator;
        this.imageRequestReferrerValidator = imageRequestReferrerValidator;
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
        imageTokenValidator.validate(token, chapterHash);
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
        imageRequestReferrerValidator.validate(request.getHeaders().getFirst(HttpHeaders.REFERER));

        ImageSpec imageRequest = new ImageSpec(ImageMode.fromPathFragment(imageMode), chapterHash, fileName);

        return imageService
            .findOrFetch(imageRequest)
            .flatMapMany(image -> {
                mangadexHeaders.addHeaders(response.getHeaders());

                response.getHeaders().setContentType(image.contentType());
                image.contentLength().ifPresent(response.getHeaders()::setContentLength);

                response.getHeaders().add("X-Cache-Mode", image.cacheMode().name());

                return image.bytes();
            });
    }

}
