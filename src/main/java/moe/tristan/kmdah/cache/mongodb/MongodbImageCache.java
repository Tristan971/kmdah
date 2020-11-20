package moe.tristan.kmdah.cache.mongodb;

import java.util.Optional;
import java.util.OptionalLong;

import org.bson.Document;
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
        String filepath = imageSpec.getPath();
        return reactiveGridFsTemplate
            .getResource(filepath)
            .flatMap(this::zipResourceAsImageContent);
    }

    @Override
    public void saveImage(ImageSpec imageSpec, ImageContent imageContent) {
        String filename = imageSpec.getPath();

        Document document = new Document();
        imageContent.contentType().ifPresent(type -> document.put(HttpHeaders.CONTENT_TYPE, type.toString()));

        reactiveGridFsTemplate
            .store(imageContent.bytes(), filename, document)
            .subscribe(id -> LOGGER.info("Stored {} in GridFS as {} (with metadata: {})", imageSpec, id, document.toString()));
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

                Optional<MediaType> contentType = Optional.empty();
                OptionalLong contentLength = OptionalLong.of(file.getLength());

                // parse metadata if exists
                Document metadata = file.getMetadata();
                if (metadata != null) {
                    contentType = Optional.ofNullable(metadata.getString(HttpHeaders.CONTENT_TYPE)).map(MediaType::parseMediaType);
                }

                return new ImageContent(
                    buffer,
                    contentType,
                    contentLength,
                    CacheMode.HIT
                );
            });
    }

}
