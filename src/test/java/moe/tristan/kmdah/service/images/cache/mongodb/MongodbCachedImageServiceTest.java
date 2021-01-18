package moe.tristan.kmdah.service.images.cache.mongodb;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;

@Testcontainers
@DirtiesContext
@ActiveProfiles("mongodb")
@SpringBootTest(classes = MongodbConfiguration.class)
class MongodbCachedImageServiceTest {

    private static final int MONGODB_PORT = 27017;

    @Container
    private static final GenericContainer<?> MONGODB = new GenericContainer<>("library/mongo:4.4")
        .withEnv("MONGO_INITDB_ROOT_USERNAME", "kmdah")
        .withEnv("MONGO_INITDB_ROOT_PASSWORD", "kmdah")
        .withExposedPorts(MONGODB_PORT);

    @Autowired
    private MongodbCachedImageService mongodbCachedImageService;

    @BeforeAll
    static void beforeAll() {
        String mongoHost = MONGODB.getHost();
        System.setProperty("KMDAH_CACHE_MONGODB_HOST", mongoHost);

        Integer mongoPort = MONGODB.getMappedPort(MONGODB_PORT);
        System.setProperty("KMDAH_CACHE_MONGODB_PORT", String.valueOf(mongoPort));
    }

    @Test
    void fileDoesntExist() {
        Optional<ImageContent> imageSearch = mongodbCachedImageService.findImage(new ImageSpec(ImageMode.DATA, "doesnot", "exist"));

        assertThat(imageSearch).isEmpty();
    }

    @Test
    void storeAndRetrieveFile() throws IOException {
        ImageSpec sampleSpec = new ImageSpec(ImageMode.DATA, "chapterid", "fileno");

        byte[] sampleBytes = requireNonNull(getClass().getClassLoader().getResourceAsStream("ref.jpg")).readAllBytes();
        MediaType sampleContentType = MediaType.IMAGE_JPEG;

        // store
        mongodbCachedImageService.saveImage(sampleSpec, sampleContentType, new ByteArrayInputStream(sampleBytes));

        // retrieve
        ImageContent retrieved = mongodbCachedImageService.findImage(sampleSpec).orElseThrow();

        assertThat(retrieved.contentLength()).hasValue(sampleBytes.length);
        assertThat(retrieved.contentType()).isEqualTo(sampleContentType);
        assertThat(retrieved.cacheMode()).isEqualTo(CacheMode.HIT);

        assertThat(retrieved.resource().getInputStream()).hasBinaryContent(sampleBytes);
    }

}
