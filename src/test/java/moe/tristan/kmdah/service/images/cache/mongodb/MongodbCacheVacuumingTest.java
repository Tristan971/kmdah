package moe.tristan.kmdah.service.images.cache.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.unit.DataSize;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;

@Testcontainers
@DirtiesContext
@ActiveProfiles("cache-mongodb")
@SpringBootTest(classes = MongodbConfiguration.class)
class MongodbCacheVacuumingTest {

    private static final int MONGODB_PORT = 27017;

    @Container
    private static final GenericContainer<?> MONGODB = new GenericContainer<>("library/mongo:4.4")
        .withEnv("MONGO_INITDB_ROOT_USERNAME", "kmdah")
        .withEnv("MONGO_INITDB_ROOT_PASSWORD", "kmdah")
        .withExposedPorts(MONGODB_PORT);

    @Autowired
    private MongoTemplate mongoTemplate;

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
    void vacuumUnnecessary() {
        VacuumingResult vacuum = mongodbCachedImageService.vacuum(new VacuumingRequest(DataSize.ofGigabytes(100)));

        assertThat(vacuum.deletedFileCount()).isZero();
        assertThat(vacuum.freedSpace().toBytes()).isZero();
    }

    @Test
    void vacuumNecessary() {
        long originalFileCount = 1000;
        long maxFileCount = 200;
        long expectedDeletedCount = originalFileCount - maxFileCount;

        // go for chunk:file 1:1 mapping (to simplify the test)
        DataSize usedSize = DataSize.ofKilobytes(originalFileCount * 255);
        DataSize maxSize = DataSize.ofKilobytes(maxFileCount * 255);

        fillCacheToSize(usedSize);

        // force collection stats to be up to date
        mongoTemplate.executeCommand(
            """
                {
                    "validate": "fs.chunks",
                    "full": true
                }
                """
        );

        VacuumingResult vacuum = mongodbCachedImageService.vacuum(new VacuumingRequest(maxSize));
        assertThat(vacuum.deletedFileCount()).isEqualTo(expectedDeletedCount);

        long remainingFileCount = mongoTemplate.count(Query.query(Criteria.where("_id").exists(true)), "fs.files");
        assertThat(remainingFileCount).isEqualTo(maxFileCount);
    }

    private void fillCacheToSize(DataSize targetSize) {
        int numberOfFilesToCreate = (int) (targetSize.toKilobytes() / 255);
        long perFile = targetSize.toBytes() / numberOfFilesToCreate;

        byte[] fileBytes = new byte[(int) perFile];
        new Random().nextBytes(fileBytes);

        for (int i = 0; i < numberOfFilesToCreate; i++) {
            ImageSpec spec = new ImageSpec(ImageMode.DATA, "test", "file-" + i);
            mongodbCachedImageService.saveImage(spec, MediaType.IMAGE_PNG, new ByteArrayInputStream(fileBytes));
        }
    }

}
