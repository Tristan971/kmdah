package moe.tristan.kmdah.cache.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.OptionalLong;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.unit.DataSize;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import moe.tristan.kmdah.cache.CacheMode;
import moe.tristan.kmdah.cache.VacuumingRequest;
import moe.tristan.kmdah.cache.VacuumingResult;
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
class MongodbCacheVacuumingTest {

    @Container
    private static final GenericContainer<?> MONGODB = new GenericContainer<>("library/mongo:4.4")
        .withEnv("MONGO_INITDB_ROOT_USERNAME", "kmdah")
        .withEnv("MONGO_INITDB_ROOT_PASSWORD", "kmdah")
        .withExposedPorts(27017);

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    private MongodbCachedImageService mongodbCachedImageService;

    @BeforeAll
    static void beforeAll() {
        String mongoHost = MONGODB.getHost();
        System.setProperty("KMDAH_CACHE_MONGODB_HOST", mongoHost);

        Integer mongoPort = MONGODB.getMappedPort(27017);
        System.setProperty("KMDAH_CACHE_MONGODB_PORT", String.valueOf(mongoPort));
    }

    @Test
    void vacuumUnnecessary() {
        Mono<VacuumingResult> vacuum = mongodbCachedImageService.vacuum(new VacuumingRequest(DataSize.ofGigabytes(100)));

        StepVerifier
            .create(vacuum)
            .consumeNextWith(result -> {
                assertThat(result.deletedFileCount()).isZero();
                assertThat(result.freedSpace().toBytes()).isZero();
            })
            .verifyComplete();
    }

    @Test
    void vacuumNecessary() {
        long originalFileCount = 100;
        long maxFileCount = 20;
        long expectedDeletedCount = originalFileCount - maxFileCount;

        // go for chunk:file 1:1 mapping (to simplify the test)
        DataSize usedSize = DataSize.ofKilobytes(originalFileCount * 255);
        DataSize maxSize = DataSize.ofKilobytes(maxFileCount * 255);

        fillCacheToSize(usedSize);

        VacuumingResult vacuum = mongodbCachedImageService
            .vacuum(new VacuumingRequest(maxSize))
            .blockOptional()
            .orElseThrow();
        assertThat(vacuum.deletedFileCount()).isEqualTo(expectedDeletedCount);

        long remainingFileCount = reactiveMongoTemplate
            .count(Query.query(Criteria.where("_id").exists(true)), "fs.files")
            .blockOptional()
            .orElseThrow();
        assertThat(remainingFileCount).isEqualTo(maxFileCount);
    }

    private void fillCacheToSize(DataSize targetSize) {
        int numberOfFilesToCreate = 100;
        long perFile = targetSize.toBytes() / numberOfFilesToCreate;

        byte[] fileBytes = new byte[(int) perFile];
        new Random().nextBytes(fileBytes);

        for (int i = 0; i < numberOfFilesToCreate; i++) {
            ImageSpec spec = new ImageSpec(ImageMode.DATA, "test", "file-" + i);
            ImageContent content = new ImageContent(
                makeRandomFileContent(fileBytes),
                MediaType.IMAGE_PNG,
                OptionalLong.of(fileBytes.length),
                CacheMode.MISS
            );

            mongodbCachedImageService.saveImage(spec, content).block();
        }
    }

    private Flux<DataBuffer> makeRandomFileContent(byte[] src) {
        return DataBufferUtils.readInputStream(
            () -> new ByteArrayInputStream(src),
            DefaultDataBufferFactory.sharedInstance,
            src.length
        );
    }

}