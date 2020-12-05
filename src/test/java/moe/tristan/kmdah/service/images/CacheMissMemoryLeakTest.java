package moe.tristan.kmdah.service.images;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import moe.tristan.kmdah.MockWebServerSupport;
import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.service.gossip.messages.LeaderImageServerEvent;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.metrics.ImageMetrics;
import okhttp3.mockwebserver.MockResponse;
import okio.Buffer;

@SpringBootTest(
    classes = {ImageService.class, MangadexImageService.class},
    properties = "io.netty.leakDetection.level=paranoid"
)
@AutoConfigureWebClient
class CacheMissMemoryLeakTest {

    private final MockWebServerSupport mockWebServerSupport = new MockWebServerSupport();

    @MockBean
    private CachedImageService cachedImageService;

    @MockBean
    private ImageMetrics imageMetrics;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ImageService imageService;

    @BeforeEach
    void setUp() throws IOException {
        String mockWebServerUri = mockWebServerSupport.start();
        applicationEventPublisher.publishEvent(new LeaderImageServerEvent(mockWebServerUri));
        when(cachedImageService.findImage(any())).thenReturn(Mono.empty());
        when(cachedImageService.saveImage(any(), any())).thenAnswer(invocation -> {
            ImageContent content = invocation.getArgument(1);
            content.bytes().subscribe();
            return Mono.empty();
        });
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServerSupport.stop();
    }

    @Test
    void manyCacheMisses() throws IOException {
        List<String> recordedLeaks = new ArrayList<>();
        ResourceLeakDetectorFactory.setResourceLeakDetectorFactory(new ResourceLeakDetectorFactory() {
            @Override
            public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval, long maxActive) {
                return new TrackingResourceLeakDetector<>(resource, samplingInterval, recordedLeaks);
            }
        });

        byte[] referenceImage = requireNonNull(getClass().getClassLoader().getResourceAsStream("ref.jpg")).readAllBytes();
        Buffer buffer = new Buffer();
        buffer.write(referenceImage);

        MockResponse mockResponse = new MockResponse();
        mockResponse.setChunkedBody(buffer, 128); // download by chunks of 128 bytes to ensure multiple buffers

        for (int i = 0; i < 100; i++) {
            mockWebServerSupport.enqueue(mockResponse);
            ImageContent content = imageService.findOrFetch(new ImageSpec(ImageMode.DATA, "chapter", "file")).block();
            requireNonNull(content).bytes().blockLast();
        }

        // try to ensure a GC happens
        Runtime.getRuntime().gc();

        assertThat(recordedLeaks).isEmpty();
    }

    public static class TrackingResourceLeakDetector<T> extends ResourceLeakDetector<T> {

        private final List<String> leaks;

        public TrackingResourceLeakDetector(Class<?> resourceType, int samplingInterval, List<String> leaks) {
            super(resourceType, samplingInterval);
            this.leaks = leaks;
        }

        @Override
        protected void reportTracedLeak(String resourceType, String records) {
            super.reportTracedLeak(resourceType, records);
            leaks.add(resourceType);
        }

        @Override
        protected void reportUntracedLeak(String resourceType) {
            super.reportUntracedLeak(resourceType);
            leaks.add(resourceType);
        }

    }

}
