package moe.tristan.kmdah.service.images.cache.filesystem;

import static moe.tristan.kmdah.service.images.cache.CacheMode.HIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.IMAGE_JPEG;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.unit.DataSize;

import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;
import moe.tristan.kmdah.service.images.cache.VacuumingResult.VacuumGranularity;

class FilesystemCachedImageServiceTest {

    private static final byte[] REFERENCE = getReferenceBytes();

    private FilesystemCachedImageService cachedImageService;

    private Path rootDir;

    @BeforeEach
    void beforeEach() throws IOException {
        rootDir = Files.createTempDirectory("fscacheservice-");
        Files.createDirectories(rootDir);
        Files.createDirectories(rootDir.resolve("data"));
        Files.createDirectories(rootDir.resolve("data-saver"));

        cachedImageService = new FilesystemCachedImageService(new FilesystemSettings(rootDir, false));
    }

    @ParameterizedTest
    @EnumSource(ImageMode.class)
    void findImage(ImageMode imageMode) throws IOException {
        ImageSpec imageSpec = writeSample(imageMode);

        awaitFile(imageSpec);

        Optional<ImageContent> findResult = cachedImageService.findImage(imageSpec);
        assertThat(findResult).isNotEmpty();

        ImageContent content = findResult.get();
        assertThat(content.resource().getInputStream()).hasBinaryContent(REFERENCE);
        assertThat(content.contentLength()).hasValue(REFERENCE.length);
        assertThat(content.contentType()).isEqualTo(IMAGE_JPEG);
        assertThat(content.cacheMode()).isEqualTo(HIT);
    }

    @ParameterizedTest
    @EnumSource(ImageMode.class)
    void saveImage(ImageMode imageMode) {
        ImageSpec imageSpec = writeSample(imageMode);

        Path path = awaitFile(imageSpec);

        assertThat(path).hasBinaryContent(REFERENCE);
    }

    @Test
    void vacuum() throws IOException {
        Set<Path> files = new HashSet<>();

        // write files
        int sampleFileCount = 20;
        for (int i = 0; i < sampleFileCount; i++) {
            ImageSpec imageSpec = writeSample(i % 2 == 0 ? ImageMode.DATA : ImageMode.DATA_SAVER);
            Path path = awaitFile(imageSpec);
            files.add(path.getParent());
        }

        assertThat(files.size()).isEqualTo(sampleFileCount);

        FileStore store = Files.getFileStore(rootDir);

        // try and make target 75% of current use, which should require a deletion of 25% of the files
        DataSize currentUsed = DataSize.ofBytes(store.getTotalSpace() - store.getUnallocatedSpace());

        double fillFactor = 1.25;
        double wantToDeleteFactor = fillFactor - 1.;

        double currentBytes = currentUsed.toBytes();
        double extraBytes = (double) currentUsed.toBytes() * wantToDeleteFactor;

        long targetBytesCount = (long) (currentBytes - extraBytes);
        DataSize targetUsed = DataSize.ofBytes(targetBytesCount);

        VacuumingResult vacuum = cachedImageService.vacuum(new VacuumingRequest(targetUsed));

        Set<Path> remainingFiles = files.stream().filter(Files::exists).collect(Collectors.toSet());

        // give some leeway to exact value
        int expectedDeletionCount = (int) (wantToDeleteFactor * (double) sampleFileCount);
        assertThat(vacuum.deletedCount()).isCloseTo(expectedDeletionCount, offset(1L));
        assertThat(remainingFiles).hasSize(files.size() - (int) vacuum.deletedCount());

        assertThat(vacuum.granularity()).isEqualTo(VacuumGranularity.CHAPTER);
    }

    private ImageSpec writeSample(ImageMode imageMode) {
        String chapter = UUID.randomUUID().toString();
        String filename = UUID.randomUUID() + ".jpeg";

        ImageSpec imageSpec = new ImageSpec(imageMode, chapter, filename);

        cachedImageService.saveImage(imageSpec, IMAGE_JPEG, new ByteArrayInputStream(REFERENCE));

        return imageSpec;
    }

    private Path awaitFile(ImageSpec imageSpec) {
        Path expectedPath = rootDir.resolve(imageSpec.mode().getPathFragment()).resolve(imageSpec.chapter()).resolve(imageSpec.file());
        await().atMost(10, TimeUnit.SECONDS).until(() -> Files.exists(expectedPath));
        return expectedPath;
    }

    private static byte[] getReferenceBytes() {
        try {
            return new ClassPathResource("ref.jpg").getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
