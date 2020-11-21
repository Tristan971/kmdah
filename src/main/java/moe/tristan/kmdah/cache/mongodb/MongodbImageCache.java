package moe.tristan.kmdah.cache.mongodb;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.OptionalLong;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.mongodb.client.gridfs.model.GridFSFile;

import moe.tristan.kmdah.cache.CacheMode;
import moe.tristan.kmdah.cache.ImageCache;
import moe.tristan.kmdah.cache.VacuumingRequest;
import moe.tristan.kmdah.cache.VacuumingResult;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;

@Component
public class MongodbImageCache implements ImageCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbImageCache.class);

    private final ReactiveGridFsTemplate reactiveGridFsTemplate;

    public MongodbImageCache(ReactiveGridFsTemplate reactiveGridFsTemplate) {
        this.reactiveGridFsTemplate = reactiveGridFsTemplate;
    }

    @Override
    public Mono<ImageContent> findImage(ImageSpec imageSpec) {
        String filename = specToFilename(imageSpec);
        return reactiveGridFsTemplate
            .getResource(filename)
            .log()
            .flatMap(this::zipResourceAsImageContent)
            .doOnNext(imageContent -> LOGGER.info("Retrieved {} from MongoDB~GridFS as {}", imageSpec, imageContent));
    }

    @Override
    public Mono<ObjectId> saveImage(ImageSpec imageSpec, ImageContent imageContent) {
        String filename = specToFilename(imageSpec);

        Document document = new Document();
        document.put(HttpHeaders.CONTENT_TYPE, imageContent.contentType().toString());

        return reactiveGridFsTemplate
            .store(imageContent.bytes(), filename, document)
            .doOnNext(id -> LOGGER.info("Stored {} in MongoDB~GridFS as _id:{} with metadata: {}", imageSpec, id, document.toString()));
    }

    @Override
    public Optional<VacuumingResult> vacuumIfNecessary(VacuumingRequest vacuumingRequest) {
        return Optional.empty();
    }

    private Mono<ImageContent> zipResourceAsImageContent(ReactiveGridFsResource resource) {
        return Mono
            .zip(resource.getGridFSFile(), Mono.just(resource.getContent()))
            .map(fileAndBuffer -> {
                GridFSFile file = fileAndBuffer.getT1();
                Flux<DataBuffer> buffer = fileAndBuffer.getT2();

                OptionalLong contentLength = OptionalLong.of(file.getLength());

                // parse metadata if exists
                Document metadata = file.getMetadata();
                String mediaType = requireNonNull(metadata).getString(HttpHeaders.CONTENT_TYPE);

                return new ImageContent(
                    buffer,
                    MediaType.parseMediaType(mediaType),
                    contentLength,
                    CacheMode.HIT
                );
            });
    }

    private String specToFilename(ImageSpec spec) {
        return String.join("/", spec.chapter(), spec.mode().name(), spec.file());
    }

}
