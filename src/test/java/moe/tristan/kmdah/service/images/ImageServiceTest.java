package moe.tristan.kmdah.service.images;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.OptionalLong;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.service.metrics.CacheModeCounter;

@SpringBootTest(classes = ImageService.class)
class ImageServiceTest {

    private static final ImageSpec SPEC = new ImageSpec(ImageMode.DATA, "chapter", "file");

    @MockBean
    private CacheModeCounter cacheModeCounter;

    @MockBean
    private CachedImageService cachedImageService;

    @MockBean
    private MangadexImageService mangadexImageService;

    @Autowired
    private ImageService imageService;

    @Test
    void onMiss() {
        ImageContent cacheMissContent = sampleContent(CacheMode.MISS);

        when(cachedImageService.findImage(eq(SPEC))).thenReturn(Mono.empty());
        when(cachedImageService.saveImage(eq(SPEC), any())).thenReturn(Mono.empty());
        when(mangadexImageService.download(eq(SPEC), any())).thenReturn(Mono.just(cacheMissContent));

        StepVerifier
            .create(imageService.findOrFetch(SPEC))
            .expectNextCount(1)
            .verifyComplete();

        verifyCachedCall();
        verifyUpstreamCall(1);

        verifyCacheModeCounted(CacheMode.MISS);
    }

    @Test
    void onHit() {
        ImageContent cacheHitContent = sampleContent(CacheMode.HIT);

        when(cachedImageService.findImage(eq(SPEC))).thenReturn(Mono.just(cacheHitContent));

        StepVerifier
            .create(imageService.findOrFetch(SPEC))
            .expectNextCount(1)
            .verifyComplete();

        verifyCachedCall();
        verifyUpstreamCall(0);

        verifyCacheModeCounted(CacheMode.HIT);
    }

    @Test
    void onFailCached() {
        ImageContent cacheMissContent = sampleContent(CacheMode.MISS);

        when(cachedImageService.findImage(eq(SPEC))).thenReturn(Mono.error(new IllegalStateException("Some underlying error!")));
        when(cachedImageService.saveImage(eq(SPEC), any())).thenReturn(Mono.empty());

        when(mangadexImageService.download(eq(SPEC), any())).thenReturn(Mono.just(cacheMissContent));

        StepVerifier
            .create(imageService.findOrFetch(SPEC))
            .expectNextCount(1)
            .verifyComplete();

        verifyCachedCall();
        verifyUpstreamCall(1);

        verifyCacheModeCounted(CacheMode.MISS);
    }

    @Test
    void onFailUpstream() {
        when(cachedImageService.findImage(eq(SPEC))).thenReturn(Mono.empty());
        when(mangadexImageService.download(eq(SPEC), any())).thenReturn(Mono.error(new IllegalStateException("Upstream error!")));

        StepVerifier
            .create(imageService.findOrFetch(SPEC))
            .expectError()
            .verify();

        verifyCachedCall();
        verifyUpstreamCall(1);

        verifyNoInteractions(cacheModeCounter);
    }

    private static ImageContent sampleContent(CacheMode cacheMode) {
        byte[] bytes = UUID.randomUUID().toString().getBytes();

        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.readInputStream(
            () -> new ByteArrayInputStream(bytes),
            DefaultDataBufferFactory.sharedInstance,
            DefaultDataBufferFactory.DEFAULT_INITIAL_CAPACITY
        );

        return new ImageContent(
            dataBufferFlux,
            MediaType.IMAGE_PNG,
            OptionalLong.of(bytes.length),
            cacheMode
        );
    }

    private void verifyCacheModeCounted(CacheMode cacheMode) {
        ArgumentCaptor<CacheMode> cacheModeCaptor = ArgumentCaptor.forClass(CacheMode.class);
        verify(cacheModeCounter).record(cacheModeCaptor.capture());
        assertThat(cacheModeCaptor.getAllValues()).containsExactly(cacheMode);
    }

    private void verifyCachedCall() {
        verify(cachedImageService, times(1)).findImage(eq(SPEC));
    }

    private void verifyUpstreamCall(int times) {
        verify(mangadexImageService, times(times)).download(eq(SPEC), any());
    }

}
