package moe.tristan.kmdah.cache.mongodb;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import moe.tristan.kmdah.cache.CacheMode;
import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;

@DataMongoTest
class MongodbImageCacheTest {

    @Test
    void fileDoesntExist(@Autowired ReactiveGridFsTemplate reactiveGridFsTemplate) {
        MongodbImageCache imageCache = new MongodbImageCache(reactiveGridFsTemplate);

        Mono<ImageContent> imageSearch = imageCache.findImage(new ImageSpec(ImageMode.DATA, "doesnot", "exist"));

        StepVerifier
            .create(imageSearch)
            .expectComplete()
            .verify();
    }

    @Test
    void storeAndRetrieveFile(@Autowired ReactiveGridFsTemplate reactiveGridFsTemplate) {
        MongodbImageCache imageCache = new MongodbImageCache(reactiveGridFsTemplate);

        ImageSpec sampleSpec = new ImageSpec(ImageMode.DATA, "chapterid", "fileno");

        byte[] sampleBytes = UUID.randomUUID().toString().getBytes();
        MediaType sampleContentType = MediaType.IMAGE_JPEG;
        ImageContent sampleImage = sampleContent(sampleBytes, sampleContentType);

        // store
        Optional<ObjectId> saveResult = imageCache
            .saveImage(sampleSpec, sampleImage)
            .blockOptional();
        assertThat(saveResult).isNotEmpty();

        // retrieve
        Optional<ImageContent> retrieval = imageCache
            .findImage(sampleSpec)
            .blockOptional();
        assertThat(retrieval).isNotEmpty();

        ImageContent retrieved = retrieval.get();
        assertThat(retrieved.contentLength()).hasValue(sampleBytes.length);
        assertThat(retrieved.contentType()).isEqualTo(sampleContentType);
        assertThat(retrieved.cacheMode()).isEqualTo(CacheMode.HIT);

        DataBuffer retrievedBytesBuffer = DataBufferUtils.join(retrieved.bytes()).block();
        InputStream retrievedBytes = requireNonNull(retrievedBytesBuffer).asInputStream();
        assertThat(retrievedBytes).hasBinaryContent(sampleBytes);
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
