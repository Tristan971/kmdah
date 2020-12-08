package moe.tristan.kmdah.mangadex.stop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import moe.tristan.kmdah.MockWebServerSupport;
import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.mangadex.MangadexSettings;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

@SpringBootTest(
    classes = StopService.class,
    properties = {
        "kmdah.mangadex.client-secret=secret",
        "kmdah.mangadex.load-balancer-ip=192.168.0.1"
    }
)
@AutoConfigureWebClient
@EnableConfigurationProperties(MangadexSettings.class)
class StopServiceTest {

    private final MockWebServerSupport mockWebServerSupport = new MockWebServerSupport();

    @MockBean
    private MangadexApi mangadexApi;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StopService stopService;

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
    void stopSuccess() throws JsonProcessingException {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(HttpStatus.OK.value());
        mockWebServerSupport.enqueue(mockResponse);

        StopRequest expectedRequest = new StopRequest("secret");

        Mono<ResponseEntity<Void>> stop = stopService.stop();
        StepVerifier
            .create(stop)
            .expectNextCount(1)
            .verifyComplete();

        RecordedRequest recordedRequest = mockWebServerSupport.takeRequest();
        String requestBody = recordedRequest.getBody().readString(StandardCharsets.UTF_8);
        assertThat(recordedRequest.getMethod()).isEqualTo(HttpMethod.POST.name());
        assertThat(objectMapper.readValue(requestBody, StopRequest.class)).isEqualTo(expectedRequest);
    }

    @Test
    void stopFailure() {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        mockWebServerSupport.enqueue(mockResponse);

        Mono<ResponseEntity<Void>> stop = stopService.stop();
        StepVerifier
            .create(stop)
            .expectError()
            .verify();
    }

}
