package moe.tristan.kmdah.mangadex.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import moe.tristan.kmdah.cache.CacheMode;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;

@SpringBootTest(classes = MangadexImageService.class)
@AutoConfigureWebClient
class MangadexImageServiceTest {

    @Autowired
    private MangadexImageService mangadexImageService;

    private final MockWebServer mockWebServer = new MockWebServer();
    private String mockWebServerUri;

    @BeforeEach
    public void setUp() throws IOException {
        mockWebServer.start();

        String hostName = mockWebServer.getHostName();
        int port = mockWebServer.getPort();
        mockWebServerUri = "http://" + hostName + ":" + port;
    }

    @AfterEach
    public void cleanup() throws IOException {
        mockWebServer.close();
    }

    @Test
    void onUpstreamSuccess() {
        MediaType contentType = MediaType.IMAGE_JPEG;
        byte[] content = UUID.randomUUID().toString().getBytes();

        mockWebServer.enqueue(responseFor(
            contentType,
            content
        ));

        ImageSpec spec = new ImageSpec(ImageMode.DATA, "chapter", "file");

        ImageContent download = mangadexImageService
            .download(spec, mockWebServerUri)
            .blockOptional()
            .orElseThrow();

        assertThat(download.contentLength()).hasValue(content.length);
        assertThat(download.contentType()).isEqualTo(contentType);
        assertThat(download.cacheMode()).isEqualTo(CacheMode.MISS);

        InputStream downloadedBytes = DataBufferUtils
            .join(download.bytes())
            .blockOptional()
            .orElseThrow()
            .asInputStream();
        assertThat(downloadedBytes).hasBinaryContent(content);
    }

    private MockResponse responseFor(MediaType mediaType, byte[] content) {
        MockResponse mockResponse = new MockResponse();

        Buffer buffer = new Buffer().write(content);
        mockResponse.setBody(buffer);
        mockResponse.setHeader(HttpHeaders.CONTENT_TYPE, mediaType.toString());

        return mockResponse;
    }

}
