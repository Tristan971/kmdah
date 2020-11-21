package moe.tristan.kmdah.mangadex.ping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;

import com.fasterxml.jackson.databind.ObjectMapper;

import moe.tristan.kmdah.MockWebServerSupport;
import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.model.settings.CacheSettings;
import moe.tristan.kmdah.model.settings.MangadexSettings;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest(
    classes = PingService.class,
    properties = {
        "kmdah.mangadex.client-secret=secret",
        "kmdah.cache.max-size-gb=100"
    }
)
@EnableConfigurationProperties({MangadexSettings.class, CacheSettings.class})
@AutoConfigureWebClient
class PingServiceTest {

    private final MockWebServerSupport mockWebServerSupport = new MockWebServerSupport();

    @MockBean
    private MangadexApi mangadexApi;

    @Autowired
    private MangadexSettings mangadexSettings;

    @Autowired
    private CacheSettings cacheSettings;

    @Autowired
    private PingService pingService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        String mockWebServerUri = mockWebServerSupport.start();
        when(mangadexApi.getApiUrl()).thenReturn(mockWebServerUri);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServerSupport.stop();
    }

    @Test
    void pingFirstTime() throws IOException {
        DataSize poolSpeed = DataSize.ofMegabytes(100 / 8);

        PingRequest expectedRequest = new PingRequest(
            mangadexSettings.clientSecret(),
            443,
            DataSize.ofGigabytes(cacheSettings.maxSizeGb()).toBytes(),
            poolSpeed.toBytes(),
            Optional.empty(),
            19
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

        MockResponse mockResponse = new MockResponse();
        mockResponse.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        mockResponse.setBody(objectMapper.writeValueAsString(expectedResponse));
        mockWebServerSupport.enqueue(mockResponse);

        PingResponse response = pingService.ping(Optional.empty(), poolSpeed).blockOptional().orElseThrow();
        assertThat(response).isEqualTo(expectedResponse);

        RecordedRequest request = mockWebServerSupport.takeRequest();
        String requestPath = request.getPath();
        String requestBody = request.getBody().readString(StandardCharsets.UTF_8);
        assertThat(requestPath).isEqualTo("/ping");
        assertThat(objectMapper.readValue(requestBody, PingRequest.class)).isEqualTo(expectedRequest);
    }

}
