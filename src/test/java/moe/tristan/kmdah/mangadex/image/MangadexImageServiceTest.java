package moe.tristan.kmdah.mangadex.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import moe.tristan.kmdah.HttpClientConfiguration;
import moe.tristan.kmdah.MockWebServerSupport;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

@SpringBootTest(classes = {
    MangadexImageService.class,
    HttpClientConfiguration.class
})
class MangadexImageServiceTest {

    private final MockWebServerSupport mockWebServerSupport = new MockWebServerSupport();
    private String mockWebServerUri;

    @Autowired
    private MangadexImageService mangadexImageService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServerUri = mockWebServerSupport.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServerSupport.stop();
    }

    @Test
    void onUpstreamSuccess() throws IOException {
        MediaType contentType = MediaType.IMAGE_JPEG;
        byte[] content = UUID.randomUUID().toString().getBytes();

        MockResponse mockResponse = new MockResponse();
        Buffer buffer = new Buffer().write(content);
        mockResponse.setBody(buffer);
        mockResponse.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        mockWebServerSupport.enqueue(mockResponse);

        ImageSpec spec = new ImageSpec(ImageMode.DATA, "chapter", "file");

        ImageContent download = mangadexImageService.download(spec, mockWebServerUri);

        assertThat(download.contentLength()).hasValue(content.length);
        assertThat(download.contentType()).isEqualTo(contentType);
        assertThat(download.cacheMode()).isEqualTo(CacheMode.MISS);

        assertThat(download.resource().getInputStream()).hasBinaryContent(content);

        RecordedRequest recordedRequest = mockWebServerSupport.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo(HttpMethod.GET.name());

        String expectedUri = "/" + spec.mode().getPathFragment() + "/" + spec.chapter() + "/" + spec.file();
        assertThat(recordedRequest.getPath()).isEqualTo(expectedUri);
    }

    @Test
    void onUpstreamFailure() {
        MockResponse mockResponse = new MockResponse();
        HttpStatus failureHttpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        mockResponse.setResponseCode(failureHttpStatus.value());
        mockWebServerSupport.enqueue(mockResponse);

        // data-saver is broken every other day amirite :^)
        ImageSpec whatever = new ImageSpec(ImageMode.DATA_SAVER, "chapter-with", "file-that-fails");
        assertThatThrownBy(() -> mangadexImageService.download(whatever, mockWebServerUri))
            .isInstanceOf(MangadexUpstreamException.class)
            .hasMessageContaining(failureHttpStatus.toString());
    }

}
