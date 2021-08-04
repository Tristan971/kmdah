package moe.tristan.kmdah.mangadex.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import moe.tristan.kmdah.HttpClientConfiguration;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.webmvc.RequestsLogger;

@SpringBootTest(classes = {
    HttpClientConfiguration.class,
    JacksonAutoConfiguration.class,
    MangadexImageService.class,
    RestTemplateAutoConfiguration.class,
    RequestsLogger.class
})
@AutoConfigureMockRestServiceServer
class MangadexImageServiceTest {

    private static final String UPSTREAM_URI = "https://upstream.domain.mangadex.network";

    private static final ImageSpec IMAGE = new ImageSpec(ImageMode.DATA, "chapter", "file");

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Autowired
    private MangadexImageService mangadexImageService;

    @Test
    void onUpstreamSuccess() throws IOException {
        MediaType contentType = MediaType.IMAGE_JPEG;
        byte[] content = UUID.randomUUID().toString().getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(content.length);

        mockRestServiceServer
            .expect(method(HttpMethod.GET))
            .andExpect(requestToUriTemplate(
                UPSTREAM_URI + "/{mode}/{chapter}/{file}",
                IMAGE.mode().getPathFragment(),
                IMAGE.chapter(),
                IMAGE.file()
            ))
            .andRespond(withStatus(HttpStatus.OK).headers(headers).body(content));

        ImageContent download = mangadexImageService.download(IMAGE, UPSTREAM_URI);

        assertThat(download.contentLength()).hasValue(content.length);
        assertThat(download.contentType()).isEqualTo(contentType);
        assertThat(download.cacheMode()).isEqualTo(CacheMode.MISS);

        assertThat(download.resource().getInputStream()).hasBinaryContent(content);
    }

    @Test
    void onUpstreamFailure() {
        HttpStatus failureHttpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        mockRestServiceServer
            .expect(method(HttpMethod.GET))
            .andExpect(requestToUriTemplate(
                UPSTREAM_URI + "/{mode}/{chapter}/{file}",
                IMAGE.mode().getPathFragment(),
                IMAGE.chapter(),
                IMAGE.file()
            ))
            .andRespond(withStatus(failureHttpStatus));

        assertThatThrownBy(() -> mangadexImageService.download(IMAGE, UPSTREAM_URI))
            .isInstanceOf(MangadexUpstreamException.class)
            .getCause()
            .hasMessageContaining("500 Internal Server Error");
    }

}
