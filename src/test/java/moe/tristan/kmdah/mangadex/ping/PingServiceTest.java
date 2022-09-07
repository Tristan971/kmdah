package moe.tristan.kmdah.mangadex.ping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.unit.DataSize;

import com.fasterxml.jackson.databind.ObjectMapper;

import moe.tristan.kmdah.HttpClientConfiguration;
import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.mangadex.MangaDexSettings;
import moe.tristan.kmdah.service.images.cache.CacheSettings;
import moe.tristan.kmdah.webmvc.RequestsLogger;

@SpringBootTest(
    classes = {
        HttpClientConfiguration.class,
        JacksonAutoConfiguration.class,
        PingService.class,
        RestTemplateAutoConfiguration.class,
        RequestsLogger.class
    },
    properties = {
        "kmdah.mangadex.client-secret=secret",
        "kmdah.mangadex.load-balancer-ip=192.168.0.1",
        "kmdah.cache.backend=delegating",
        "kmdah.cache.max-size-gb=100",
    }
)
@AutoConfigureMockRestServiceServer
@EnableConfigurationProperties({MangaDexSettings.class, CacheSettings.class})
class PingServiceTest {

    @MockBean
    private MangadexApi mangadexApi;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Autowired
    private MangaDexSettings mangadexSettings;

    @Autowired
    private CacheSettings cacheSettings;

    @Autowired
    private PingService pingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    @BeforeEach
    void setUp() {
        when(mangadexApi.getApiUrl()).thenReturn("https://api.mangadex.network");
    }

    @Test
    void pingFirstTime() throws IOException {
        DataSize poolSpeed = DataSize.ofMegabytes(100 / 8);

        //noinspection ConstantConditions
        PingRequest expectedRequest = new PingRequest(
            mangadexSettings.clientSecret(),
            mangadexSettings.loadBalancerIp(),
            443,
            DataSize.ofGigabytes(cacheSettings.maxSizeGb()).toBytes(),
            poolSpeed.toBytes(),
            Optional.empty(),
            environment.getProperty("spring.application.spec", Integer.class)
        );

        PingResponse expectedResponse = new PingResponse(
            "https://image-server.mangadex.org",
            "latest-build",
            "https://client-uri.mangadex.network",
            "token-key",
            false,
            false,
            Optional.empty()
        );

        mockRestServiceServer
            .expect(method(HttpMethod.POST))
            .andExpect(requestTo(mangadexApi.getApiUrl() + "/ping"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(expectedRequest)))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(expectedResponse))
            );

        PingResponse response = pingService.ping(Optional.empty(), poolSpeed);
        assertThat(response).isEqualTo(expectedResponse);

        mockRestServiceServer.verify();
    }

}
