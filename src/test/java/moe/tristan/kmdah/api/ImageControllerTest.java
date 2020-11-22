package moe.tristan.kmdah.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.OptionalLong;
import java.util.UUID;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import moe.tristan.kmdah.cache.CacheMode;
import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.mangadex.image.MangadexHeaders;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;
import moe.tristan.kmdah.service.images.ImageService;

@WebFluxTest(ImageController.class)
class ImageControllerTest {

    @MockBean
    private ImageService imageService;

    @MockBean
    private MangadexHeaders mangadexHeaders;

    @MockBean
    private ImageTokenValidator imageTokenValidator;

    @MockBean
    private ImageRequestReferrerValidator imageRequestReferrerValidator;

    @Autowired
    private WebTestClient webTestClient;

    @ParameterizedTest
    @EnumSource(CacheMode.class)
    void onSuccess(CacheMode cacheMode) {
        String token = "sampletoken";
        String referrer = "referrer";

        ImageSpec sample = new ImageSpec(ImageMode.DATA, "chapter", "file");

        String expectedContent = UUID.randomUUID().toString();
        MediaType mediaType = MediaType.IMAGE_PNG;

        ImageContent sampleContent = sampleContent(expectedContent.getBytes(), mediaType, OptionalLong.empty(), cacheMode);

        when(imageService.findOrFetch(eq(sample))).thenReturn(Mono.just(sampleContent));

        webTestClient
            .get()
            .uri("/{token}/{mode}/{chapter}/{file}", token, sample.mode().getPathFragment(), sample.chapter(), sample.file())
            .header(HttpHeaders.REFERER, referrer)
            .exchange()
            .expectHeader().valueEquals("X-Cache-Mode", cacheMode.name())
            .expectBody(String.class)
            .consumeWith(result -> {
                String content = result.getResponseBody();
                assertThat(content).isEqualTo(expectedContent);
            });

        verify(mangadexHeaders).addHeaders(any());
        verify(imageTokenValidator).validate(eq(token), eq(sample.chapter()));
        verify(imageRequestReferrerValidator).validate(eq(referrer));
    }

    private ImageContent sampleContent(byte[] contentBytes, MediaType mediaType, OptionalLong contentLength, CacheMode cacheMode) {
        Flux<DataBuffer> content = DataBufferUtils.readInputStream(
            () -> new ByteArrayInputStream(contentBytes),
            DefaultDataBufferFactory.sharedInstance,
            contentBytes.length
        );

        return new ImageContent(
            content,
            mediaType,
            contentLength,
            cacheMode
        );
    }

}
