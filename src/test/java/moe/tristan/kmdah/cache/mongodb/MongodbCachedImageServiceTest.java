package moe.tristan.kmdah.cache.mongodb;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.OptionalLong;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import moe.tristan.kmdah.cache.CacheMode;
import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;

@SpringBootTest(classes = {
    MongodbCachedImageService.class,
    MongoReactiveAutoConfiguration.class,
    MongoReactiveDataAutoConfiguration.class
})
@Testcontainers
@DirtiesContext
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
        Mono<ImageContent> imageSearch = mongodbCachedImageService.findImage(new ImageSpec(ImageMode.DATA, "doesnot", "exist"));

        StepVerifier
            .create(imageSearch)
            .expectComplete()
            .verify();
    }

    @Test
    void storeAndRetrieveFile() throws IOException {
        ImageSpec sampleSpec = new ImageSpec(ImageMode.DATA, "chapterid", "fileno");

        byte[] sampleBytes = requireNonNull(getClass().getClassLoader().getResourceAsStream("ref.jpg")).readAllBytes();
        MediaType sampleContentType = MediaType.IMAGE_JPEG;
        ImageContent sampleImage = sampleContent(sampleBytes, sampleContentType);

        // store
        ObjectId saveResult = mongodbCachedImageService.saveImage(sampleSpec, sampleImage).blockOptional().orElseThrow();
        assertThat(saveResult).isNotNull();

        // retrieve
        ImageContent retrieved = mongodbCachedImageService.findImage(sampleSpec).blockOptional().orElseThrow();

        assertThat(retrieved.contentLength()).hasValue(sampleBytes.length);
        assertThat(retrieved.contentType()).isEqualTo(sampleContentType);
        assertThat(retrieved.cacheMode()).isEqualTo(CacheMode.HIT);

        DataBuffer retrievedBytesBuffer = DataBufferUtils.join(retrieved.bytes()).block();
        byte[] retrievedBytes = requireNonNull(retrievedBytesBuffer).asInputStream().readAllBytes();
        assertThat(retrievedBytes).isEqualTo(sampleBytes);
    }

    private ImageContent sampleContent(byte[] bytes, MediaType mediaType) {
        long len = bytes.length;

        Flux<DataBuffer> bytesBuffer = DataBufferUtils.readInputStream(
            () -> new ByteArrayInputStream(bytes),
            new DefaultDataBufferFactory(false),
            bytes.length / 2 // ensure chunked
        );

        return new ImageContent(bytesBuffer, mediaType, OptionalLong.of(len), CacheMode.MISS);
    }

}
