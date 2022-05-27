package moe.tristan.kmdah.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.REFERER;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.OptionalLong;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import moe.tristan.kmdah.mangadex.MangadexSettings;
import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageService;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.validation.ImageRequestReferrerValidator;
import moe.tristan.kmdah.service.images.validation.ImageRequestTokenValidator;
import moe.tristan.kmdah.service.images.validation.InvalidImageRequestReferrerException;
import moe.tristan.kmdah.service.images.validation.InvalidImageRequestTokenException;
import moe.tristan.kmdah.service.metrics.ImageMetrics;
import moe.tristan.kmdah.service.metrics.geoip.GeoIpMetrics;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

    @MockBean
    private ImageService imageService;

    @MockBean
    private ImageRequestTokenValidator imageRequestTokenValidator;

    @MockBean
    private ImageRequestReferrerValidator imageRequestReferrerValidator;

    @MockBean
    private ImageControllerHeaders imageControllerHeaders;

    @MockBean
    private GeoIpMetrics geoIpMetrics;

    @MockBean
    private ImageMetrics imageMetrics;

    @MockBean
    private MangadexSettings mangadexSettings;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        when(mangadexSettings.enforceTokens()).thenReturn(true);
    }

    @Test
    void onSuccess() throws Exception {
        String token = "sampletoken";
        String referrer = "referrer";

        ImageSpec sample = new ImageSpec(ImageMode.DATA, "chapter", "file.jpeg");

        String expectedContent = UUID.randomUUID().toString();
        MediaType mediaType = MediaType.IMAGE_PNG;

        ImageContent sampleContent = sampleContent(expectedContent.getBytes(), mediaType, OptionalLong.empty());

        when(imageService.findOrFetch(eq(sample))).thenReturn(sampleContent);

        mockMvc.perform(request(
                   GET, "/{token}/{mode}/{chapter}/{file}",
                   token, sample.mode().getPathFragment(), sample.chapter(), sample.file()
               ).header(REFERER, referrer))
               .andExpect(status().isOk())
               .andExpect(content().string(expectedContent));

        verify(imageRequestTokenValidator).validate(eq(token), eq(sample.chapter()));
        verify(imageRequestReferrerValidator).validate(eq(referrer));
    }

    @Test
    void onInvalidToken() throws Exception {
        String token = "sampletoken";

        ImageSpec sample = new ImageSpec(ImageMode.DATA, "chapter", "file.jpeg");

        doThrow(new InvalidImageRequestTokenException(token)).when(imageRequestTokenValidator).validate(eq(token), eq(sample.chapter()));

        mockMvc
            .perform(request(GET, "/{token}/{mode}/{chapter}/{file}", token, sample.mode().getPathFragment(), sample.chapter(), sample.file()))
            .andExpect(status().isForbidden());
    }

    @Test
    void onMalicious() throws Exception {
        String token = "sampletoken";

        ImageSpec sample = new ImageSpec(ImageMode.DATA, "chapter", "..");
        mockMvc.perform(request(GET, "/{token}/{mode}/{chapter}/{file}", token, sample.mode().getPathFragment(), sample.chapter(), sample.file()))
               .andExpect(status().isBadRequest());
    }

    @Test
    void onInvalidReferrerHeader() throws Exception {
        String referrer = "referrer";

        ImageSpec sample = new ImageSpec(ImageMode.DATA, "chapter", "file.jpeg");

        doThrow(new InvalidImageRequestReferrerException(referrer)).when(imageRequestReferrerValidator).validate(eq(referrer));

        mockMvc.perform(
            request(GET, "/{token}/{mode}/{chapter}/{file}", "whatever", sample.mode().getPathFragment(), sample.chapter(), sample.file())
                .header(REFERER, referrer)
        ).andExpect(
            status().isForbidden()
        );
    }

    private ImageContent sampleContent(byte[] contentBytes, MediaType mediaType, OptionalLong contentLength) {
        return new ImageContent(
            new InputStreamResource(new ByteArrayInputStream(contentBytes)),
            mediaType,
            contentLength,
            Instant.now(),
            CacheMode.HIT
        );
    }

}
