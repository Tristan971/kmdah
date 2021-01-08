package moe.tristan.kmdah.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.OptionalLong;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.StreamUtils;

import moe.tristan.kmdah.service.gossip.InstanceId;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.cache.CacheMode;

@SpringBootTest(
    classes = {
        ImageControllerHeaders.class,
        ImageControllerHeadersTest.SampleInstanceIdConfiguration.class
    },
    properties = {
        "spring.application.version=" + ImageControllerHeadersTest.VERSION,
        "spring.application.spec=" + ImageControllerHeadersTest.SPEC
    }
)
class ImageControllerHeadersTest {

    static final String VERSION = "sample-version";
    static final String SPEC = "69";
    private static final Instant LAST_MODIFIED = LocalDate.of(1996, 1, 10).atStartOfDay().toInstant(ZoneOffset.UTC);

    @Autowired
    private InstanceId instanceId;

    @Autowired
    private ImageControllerHeaders imageControllerHeaders;

    @ParameterizedTest
    @EnumSource(CacheMode.class)
    void validateHeadersByCacheMode(CacheMode cacheMode) throws ParseException {
        MockHttpServletResponse sampleResponse = new MockHttpServletResponse();

        ImageContent content = new ImageContent(
            new InputStreamResource(StreamUtils.emptyInput()),
            MediaType.IMAGE_JPEG,
            OptionalLong.of(1L),
            LAST_MODIFIED,
            cacheMode
        );
        imageControllerHeaders.addHeaders(sampleResponse, content);

        validateInstanceId(sampleResponse, instanceId);
        validateCacheMode(sampleResponse, content.cacheMode());
        validateContentLength(sampleResponse, content.contentLength());
        validateMangadexHeadersPresent(sampleResponse);
    }

    private void validateInstanceId(HttpServletResponse response, InstanceId instanceId) {
        assertThat(response.getHeader("X-Instance-Id")).isEqualTo(instanceId.id());
    }

    private void validateCacheMode(HttpServletResponse response, CacheMode cacheMode) {
        assertThat(response.getHeader("X-Cache")).isEqualTo(cacheMode.name());
    }

    private void validateContentLength(HttpServletResponse response, OptionalLong upstreamLength) {
        upstreamLength.ifPresent(length -> assertThat(response.getHeader(HttpHeaders.CONTENT_LENGTH)).isEqualTo(String.valueOf(length)));
    }

    private void validateMangadexHeadersPresent(HttpServletResponse response) throws ParseException {
        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo("https://mangadex.org");
        assertThat(response.getHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS)).isEqualTo("*");
        assertThat(response.getHeader(HttpHeaders.CACHE_CONTROL)).isEqualTo("public/ max-age=1209600");
        assertThat(response.getHeader("Timing-Allow-Origin")).isEqualTo("https://mangadex.org");
        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");

        String lastModified = response.getHeader(HttpHeaders.LAST_MODIFIED);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormatUtils.SMTP_DATETIME_FORMAT.getPattern());
        Date date = dateFormat.parse(lastModified);
        assertThat(date.toInstant()).isEqualTo(LAST_MODIFIED);
    }

    @TestConfiguration
    static class SampleInstanceIdConfiguration {

        @Bean
        InstanceId instanceId() {
            return new InstanceId(UUID.randomUUID().toString());
        }

    }

}
