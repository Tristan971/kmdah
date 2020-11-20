package moe.tristan.kmdah.cache.mongodb;

import java.io.ByteArrayInputStream;
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

        ImageSpec spec = new ImageSpec(ImageMode.DATA, "chapterid", "fileno");

        byte[] sampleBytes = UUID.randomUUID().toString().getBytes();
        MediaType mediaType = MediaType.IMAGE_JPEG;
        long len = sampleBytes.length;

        Flux<DataBuffer> bytesBuffer = DataBufferUtils.readInputStream(
            () -> new ByteArrayInputStream(sampleBytes),
            new DefaultDataBufferFactory(false),
            sampleBytes.length / 2 // ensure chunked
        );

        ImageContent content = new ImageContent(bytesBuffer, Optional.of(mediaType), OptionalLong.of(len), CacheMode.MISS);

        Mono<ObjectId> saveResult = imageCache.saveImage(spec, content);

        StepVerifier
            .create(saveResult)
            .expectNextCount(1)
            .verifyComplete();
    }

}
