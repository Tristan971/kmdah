package moe.tristan.kmdah.mangadex.stop;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import moe.tristan.kmdah.HttpClientConfiguration;
import moe.tristan.kmdah.mangadex.MangadexApi;
import moe.tristan.kmdah.mangadex.MangadexSettings;
import moe.tristan.kmdah.webmvc.RequestsLogger;

@SpringBootTest(
    classes = {
        JacksonAutoConfiguration.class,
        HttpClientConfiguration.class,
        RequestsLogger.class,
        RestTemplateAutoConfiguration.class,
        StopService.class
    },
    properties = {
        "kmdah.mangadex.client-secret=secret",
        "kmdah.mangadex.load-balancer-ip=192.168.0.1"
    }
)
@AutoConfigureMockRestServiceServer
@EnableConfigurationProperties(MangadexSettings.class)
class StopServiceTest {

    @MockBean
    private MangadexApi mangadexApi;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StopService stopService;

    @BeforeEach
    void setUp() {
        when(mangadexApi.getApiUrl()).thenReturn("https://api.mangadex.network");
    }

    @Test
    void stopSuccess() throws JsonProcessingException {
        StopRequest expectedRequest = new StopRequest("secret");

        mockRestServiceServer
            .expect(method(HttpMethod.POST))
            .andExpect(requestTo(mangadexApi.getApiUrl() + "/stop"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(expectedRequest)))
            .andRespond(withStatus(HttpStatus.OK));

        stopService.stop();

        mockRestServiceServer.verify();
    }

    @Test
    void stopFailure() throws JsonProcessingException {
        StopRequest expectedRequest = new StopRequest("secret");

        mockRestServiceServer
            .expect(method(HttpMethod.POST))
            .andExpect(requestTo(mangadexApi.getApiUrl() + "/stop"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(expectedRequest)))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(stopService::stop).hasMessageContaining(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        mockRestServiceServer.verify();
    }

}
