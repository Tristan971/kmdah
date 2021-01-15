package moe.tristan.kmdah.service.images;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;

import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.metrics.ImageMetrics;

@SpringBootTest(classes = ImageService.class)
class ImageServiceTest {

    private static final ImageSpec SPEC = new ImageSpec(ImageMode.DATA, "chapter", "file");

    @MockBean
    private CachedImageService cachedImageService;

    @MockBean
    private MangadexImageService mangadexImageService;

    @MockBean
    private ImageMetrics imageMetrics;

    @Autowired
    private ImageService imageService;

    @Test
    void onMiss() {
        ImageContent cacheMissContent = sampleContent(CacheMode.MISS);

        when(cachedImageService.findImage(eq(SPEC))).thenReturn(Optional.empty());
        when(mangadexImageService.download(eq(SPEC), any())).thenReturn(cacheMissContent);

        imageService.findOrFetch(SPEC);

        verifyCachedCall();
        verifyUpstreamCall(1);

        verifyCacheModeCounted(CacheMode.MISS);
    }

    @Test
    void onHit() {
        ImageContent cacheHitContent = sampleContent(CacheMode.HIT);

        when(cachedImageService.findImage(eq(SPEC))).thenReturn(Optional.of(cacheHitContent));

        imageService.findOrFetch(SPEC);

        verifyCachedCall();
        verifyUpstreamCall(0);

        verifyCacheModeCounted(CacheMode.HIT);
    }

    @Test
    void onFailCached() {
        ImageContent cacheMissContent = sampleContent(CacheMode.MISS);

        when(cachedImageService.findImage(eq(SPEC))).thenThrow(new IllegalStateException("Some underlying error!"));
        when(mangadexImageService.download(eq(SPEC), any())).thenReturn(cacheMissContent);

        imageService.findOrFetch(SPEC);

        verifyCachedCall();
        verifyUpstreamCall(1);

        verifyCacheModeCounted(CacheMode.MISS);
    }

    @Test
    void onFailUpstream() {
        when(cachedImageService.findImage(eq(SPEC))).thenReturn(Optional.empty());
        IllegalStateException upstreamException = new IllegalStateException("Upstream error!");
        when(mangadexImageService.download(eq(SPEC), any())).thenThrow(upstreamException);

        assertThatThrownBy(() -> imageService.findOrFetch(SPEC)).isEqualTo(upstreamException);

        verifyCachedCall();
        verifyUpstreamCall(1);
    }

    private static ImageContent sampleContent(CacheMode cacheMode) {
        byte[] bytes = UUID.randomUUID().toString().getBytes();

        return new ImageContent(
            new InputStreamResource(new ByteArrayInputStream(bytes)),
            MediaType.IMAGE_PNG,
            OptionalLong.of(bytes.length),
            Instant.now(),
            cacheMode
        );
    }

    private void verifyCacheModeCounted(CacheMode cacheMode) {
        ArgumentCaptor<CacheMode> cacheModeCaptor = ArgumentCaptor.forClass(CacheMode.class);
        verify(imageMetrics).recordSearch(anyLong(), cacheModeCaptor.capture());
        assertThat(cacheModeCaptor.getAllValues()).containsExactly(cacheMode);
    }

    private void verifyCachedCall() {
        verify(cachedImageService, times(1)).findImage(eq(SPEC));
    }

    private void verifyUpstreamCall(int times) {
        verify(mangadexImageService, times(times)).download(eq(SPEC), any());
    }

}
