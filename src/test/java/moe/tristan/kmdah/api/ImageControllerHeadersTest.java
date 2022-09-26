package moe.tristan.kmdah.api;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.OptionalLong;
import java.util.UUID;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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
    void validateHeadersByCacheMode(CacheMode cacheMode) {
        ImageContent content = new ImageContent(
            new InputStreamResource(InputStream.nullInputStream()),
            MediaType.IMAGE_JPEG,
            OptionalLong.of(1L),
            LAST_MODIFIED,
            cacheMode
        );
        HttpHeaders headers = imageControllerHeaders.buildHeaders(content);

        validateInstanceId(headers, instanceId);
        validateCacheMode(headers, content.cacheMode());
        validateContentLength(headers, content.contentLength());
        validateMangadexHeadersPresent(headers);
    }

    private void validateInstanceId(HttpHeaders headers, InstanceId instanceId) {
        assertThat(headers.getFirst("X-Instance-Id")).isEqualTo(instanceId.id());
    }

    private void validateCacheMode(HttpHeaders headers, CacheMode cacheMode) {
        assertThat(headers.getFirst("X-Cache")).isEqualTo(cacheMode.name());
    }

    private void validateContentLength(HttpHeaders headers, OptionalLong upstreamLength) {
        upstreamLength.ifPresent(
            length -> assertThat(headers.getContentLength()).isEqualTo(length)
        );
    }

    private void validateMangadexHeadersPresent(HttpHeaders headers) {
        assertThat(headers.getAccessControlAllowOrigin()).isEqualTo("*");
        assertThat(headers.getAccessControlExposeHeaders()).containsExactly("*");
        assertThat(requireNonNull(headers.getCacheControl()).split(", ")).containsExactlyInAnyOrder("public", "max-age=1209600");
        assertThat(headers.getFirst("Timing-Allow-Origin")).isEqualTo("*");
        assertThat(headers.getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(headers.getLastModified()).isEqualTo(LAST_MODIFIED.toEpochMilli());
    }

    @TestConfiguration
    static class SampleInstanceIdConfiguration {

        @Bean
        InstanceId instanceId() {
            return new InstanceId(UUID.randomUUID().toString());
        }

    }

}
