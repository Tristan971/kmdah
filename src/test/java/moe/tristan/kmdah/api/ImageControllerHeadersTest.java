package moe.tristan.kmdah.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.OptionalLong;
import java.util.UUID;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

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

    public static final String VERSION = "sample-version";
    public static final String SPEC = "69";

    @Autowired
    private InstanceId instanceId;

    @Autowired
    private ImageControllerHeaders imageControllerHeaders;

    @ParameterizedTest
    @EnumSource(CacheMode.class)
    void validateHeadersByCacheMode(CacheMode cacheMode) {
        HttpHeaders headers = new HttpHeaders();

        ImageContent content = new ImageContent(Flux.empty(), MediaType.IMAGE_JPEG, OptionalLong.of(1L), cacheMode);
        imageControllerHeaders.addHeaders(headers, content);

        validateInstanceId(headers, instanceId);
        validateCacheMode(headers, content.cacheMode());
        validateContentLength(headers, content.contentLength());
        validateMangadexHeadersPresent(headers);
        validateServerHeader(headers);
    }

    private void validateServerHeader(HttpHeaders headers) {
        String expected = "kmdah " + VERSION + " (" + SPEC + ") - github.com/Tristan971/kmdah";
        assertThat(headers.get(HttpHeaders.SERVER))
            .containsExactly(expected);
    }

    private void validateInstanceId(HttpHeaders headers, InstanceId instanceId) {
        assertThat(headers.get("X-Instance-Id"))
            .containsExactly(instanceId.id());
    }

    private void validateCacheMode(HttpHeaders headers, CacheMode cacheMode) {
        assertThat(headers.get("X-Cache"))
            .containsExactly(cacheMode.name());
        assertThat(headers.get("X-Cache-Mode"))
            .containsExactly(cacheMode.name());
    }

    private void validateContentLength(HttpHeaders headers, OptionalLong upstreamLength) {
        upstreamLength.ifPresent(length -> assertThat(headers.getContentLength()).isEqualTo(length));
    }

    private void validateMangadexHeadersPresent(HttpHeaders headers) {
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
            .containsExactly("https://mangadex.org");

        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS))
            .containsExactly("*");

        assertThat(headers.get(HttpHeaders.CACHE_CONTROL))
            .containsExactly("public/ max-age=1209600");

        assertThat(headers.get("Timing-Allow-Origin"))
            .containsExactly("https://mangadex.org");

        assertThat(headers.get("X-Content-Type-Options"))
            .containsExactly("nosniff");

        assertThat(headers.get("X-Content-Type-Options"))
            .containsExactly("nosniff");
    }

    @TestConfiguration
    static class SampleInstanceIdConfiguration {

        @Bean
        InstanceId instanceId() {
            return new InstanceId(UUID.randomUUID().toString());
        }

    }

}
