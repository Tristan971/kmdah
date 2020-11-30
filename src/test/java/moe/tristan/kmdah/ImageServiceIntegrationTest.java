package moe.tristan.kmdah;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebFlux;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import moe.tristan.kmdah.api.ImageController;
import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.service.gossip.messages.LeaderImageServerEvent;
import moe.tristan.kmdah.service.images.ImageService;
import moe.tristan.kmdah.service.images.cache.mongodb.MongodbCachedImageService;
import moe.tristan.kmdah.service.images.validation.ImageRequestReferrerValidator;
import moe.tristan.kmdah.service.images.validation.ImageRequestTokenValidator;
import moe.tristan.kmdah.service.metrics.CacheModeCounter;
import okhttp3.mockwebserver.MockResponse;
import okio.Buffer;

/**
 * That's a THICC test because the reactor threading model is so alien I'm too stupid to grasp it well enough to have confidence in the unit tests alone.
 */
@SpringBootTest(
    classes = {
        ImageController.class,
        ImageService.class,
        MangadexImageService.class,
        MongodbCachedImageService.class,
        MongoReactiveAutoConfiguration.class,
        MongoReactiveDataAutoConfiguration.class
    },
    properties = "spring.codec.max-in-memory-size=5242880" // allow webtestclient to buffer 5MB of body size
)
@Testcontainers
@DirtiesContext
@AutoConfigureWebFlux
@AutoConfigureWebTestClient
@AutoConfigureWebClient
public class ImageServiceIntegrationTest {

    private static final int MONGODB_PORT = 27017;

    @Container
    private static final GenericContainer<?> MONGODB = new GenericContainer<>("library/mongo:4.4")
        .withEnv("MONGO_INITDB_ROOT_USERNAME", "kmdah")
        .withEnv("MONGO_INITDB_ROOT_PASSWORD", "kmdah")
        .withExposedPorts(MONGODB_PORT);

    private final MockWebServerSupport mockWebServerSupport = new MockWebServerSupport();

    @MockBean
    private CacheModeCounter cacheModeCounter;

    @MockBean
    private ImageRequestReferrerValidator requestReferrerValidator;

    @MockBean
    private ImageRequestTokenValidator requestTokenValidator;

    @Autowired
    private ImageService imageService;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void beforeAll() {
        String mongoHost = MONGODB.getHost();
        System.setProperty("KMDAH_CACHE_MONGODB_HOST", mongoHost);

        Integer mongoPort = MONGODB.getMappedPort(MONGODB_PORT);
        System.setProperty("KMDAH_CACHE_MONGODB_PORT", String.valueOf(mongoPort));
    }

    @BeforeEach
    void setUp() throws IOException {
        String mockWebServerUri = mockWebServerSupport.start();
        imageService.onLeaderImageServerEvent(new LeaderImageServerEvent(mockWebServerUri));
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServerSupport.stop();
    }

    @Test
    void missThenHit() throws IOException {
        Resource reference = new ClassPathResource("ref.jpg", getClass().getClassLoader());
        byte[] expected = reference.getInputStream().readAllBytes();

        Buffer referenceUpstreamBody = new Buffer();
        referenceUpstreamBody.write(expected);
        MockResponse referenceUpstreamResponse = new MockResponse();
        referenceUpstreamResponse.setBody(referenceUpstreamBody);
        referenceUpstreamResponse.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);

        IntStream.range(0, 100).parallel().forEach(i -> {
            mockWebServerSupport.enqueue(referenceUpstreamResponse);
            sendRequestAndExpect(expected);
        });
    }

    private void sendRequestAndExpect(byte[] expectedContent) {
        webTestClient
            .get()
            .uri("/data/chapter/file.jpg")
            .exchange()
            .expectBody(byte[].class)
            .value(result -> assertThat(result).isEqualTo(expectedContent));
    }

}
