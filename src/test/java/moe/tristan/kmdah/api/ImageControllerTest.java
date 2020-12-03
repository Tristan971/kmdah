package moe.tristan.kmdah.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.OptionalLong;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageService;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.validation.ImageRequestReferrerValidator;
import moe.tristan.kmdah.service.images.validation.ImageRequestTokenValidator;
import moe.tristan.kmdah.service.images.validation.InvalidImageRequestReferrerException;
import moe.tristan.kmdah.service.images.validation.InvalidImageRequestTokenException;

@WebFluxTest(ImageController.class)
class ImageControllerTest {

    @MockBean
    private ImageService imageService;

    @MockBean
    private ImageRequestTokenValidator imageRequestTokenValidator;

    @MockBean
    private ImageRequestReferrerValidator imageRequestReferrerValidator;

    @MockBean
    private ImageControllerHeaders imageControllerHeaders;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void onSuccess() {
        String token = "sampletoken";
        String referrer = "referrer";

        ImageSpec sample = new ImageSpec(ImageMode.DATA, "chapter", "file");

        String expectedContent = UUID.randomUUID().toString();
        MediaType mediaType = MediaType.IMAGE_PNG;

        ImageContent sampleContent = sampleContent(expectedContent.getBytes(), mediaType, OptionalLong.empty());

        when(imageService.findOrFetch(eq(sample))).thenReturn(Mono.just(sampleContent));

        webTestClient
            .get()
            .uri("/{token}/{mode}/{chapter}/{file}", token, sample.mode().getPathFragment(), sample.chapter(), sample.file())
            .header(HttpHeaders.REFERER, referrer)
            .exchange()
            .expectBody(String.class)
            .consumeWith(result -> {
                String content = result.getResponseBody();
                assertThat(content).isEqualTo(expectedContent);
            });

        verify(imageRequestTokenValidator).validate(eq(token), eq(sample.chapter()));
        verify(imageRequestReferrerValidator).validate(eq(referrer));
    }

    @Test
    void onErrorFromImageService() {
        String token = "sampletoken";
        String referrer = "referrer";

        ImageSpec sample = new ImageSpec(ImageMode.DATA, "chapter", "file");

        when(imageService.findOrFetch(eq(sample))).thenReturn(Mono.error(new IllegalStateException("Underlying error")));

        webTestClient
            .get()
            .uri("/{token}/{mode}/{chapter}/{file}", token, sample.mode().getPathFragment(), sample.chapter(), sample.file())
            .header(HttpHeaders.REFERER, referrer)
            .exchange()
            .expectStatus().is5xxServerError();
    }

    @Test
    void onInvalidToken() {
        String token = "sampletoken";

        ImageSpec sample = new ImageSpec(ImageMode.DATA, "chapter", "file");

        doThrow(new InvalidImageRequestTokenException(token))
            .when(imageRequestTokenValidator).validate(eq(token), eq(sample.chapter()));

        webTestClient
            .get()
            .uri("/{token}/{mode}/{chapter}/{file}", token, sample.mode().getPathFragment(), sample.chapter(), sample.file())
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void onInvalidReferrerHeader() {
        String referrer = "referrer";

        ImageSpec sample = new ImageSpec(ImageMode.DATA, "chapter", "file");

        doThrow(new InvalidImageRequestReferrerException(referrer))
            .when(imageRequestReferrerValidator).validate(eq(referrer));

        webTestClient
            .get()
            .uri("/{token}/{mode}/{chapter}/{file}", "sometoken", sample.mode().getPathFragment(), sample.chapter(), sample.file())
            .header(HttpHeaders.REFERER, referrer)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ImageContent sampleContent(byte[] contentBytes, MediaType mediaType, OptionalLong contentLength) {
        Flux<DataBuffer> content = DataBufferUtils.readInputStream(
            () -> new ByteArrayInputStream(contentBytes),
            DefaultDataBufferFactory.sharedInstance,
            contentBytes.length
        );

        return new ImageContent(
            content,
            mediaType,
            contentLength,
            CacheMode.HIT
        );
    }

}
