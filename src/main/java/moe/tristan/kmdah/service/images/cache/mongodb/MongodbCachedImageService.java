package moe.tristan.kmdah.service.images.cache.mongodb;

import static java.lang.Math.toIntExact;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.whereFilename;

import java.util.OptionalLong;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Mono;

import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;

@Component
public class MongodbCachedImageService implements CachedImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbCachedImageService.class);

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ReactiveGridFsTemplate reactiveGridFsTemplate;

    public MongodbCachedImageService(ReactiveGridFsTemplate reactiveGridFsTemplate, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.reactiveGridFsTemplate = reactiveGridFsTemplate;
    }

    @Override
    public Mono<ImageContent> findImage(ImageSpec imageSpec) {
        String filename = specToFilename(imageSpec);
        return reactiveGridFsTemplate
            .findFirst(query(whereFilename().is(filename)))
            .map(gridFSFile -> {
                OptionalLong contentLength = OptionalLong.of(gridFSFile.getLength());

                String mediaType = requireNonNull(gridFSFile.getMetadata()).getString("_contentType");

                return new ImageContent(
                    reactiveGridFsTemplate.getResource(gridFSFile).flatMapMany(ReactiveGridFsResource::getContent).share(),
                    MediaType.parseMediaType(mediaType),
                    contentLength,
                    CacheMode.HIT
                );
            })
            .doOnNext(imageContent -> LOGGER.debug("Retrieved {} from MongoDB~GridFS as {}", imageSpec, imageContent));
    }

    @Override
    public Mono<ObjectId> saveImage(ImageSpec imageSpec, ImageContent imageContent) {
        String filename = specToFilename(imageSpec);

        LOGGER.debug("Storing {} in MongoDB~GridFS as {} with content type: {}", imageSpec, filename, imageContent.contentType());

        return reactiveGridFsTemplate
            .store(imageContent.bytes(), filename, imageContent.contentType().toString())
            .doOnSuccess(objectId -> LOGGER.debug(
                "Stored {} in MongoDB~GridFS as _id:{}/{} with content type: {}",
                imageSpec,
                objectId,
                filename,
                imageContent.contentType()
            ))
            .doOnError(err -> LOGGER.error(
                "Failed storing {} in MongoFB~GridFS as {} with content type: {}",
                imageSpec,
                filename,
                imageContent.contentType(),
                err
            ));
    }

    @Override
    public Mono<VacuumingResult> vacuum(VacuumingRequest vacuumingRequest) {
        return reactiveMongoTemplate.executeCommand(
            """
                {
                    "collStats": "fs.chunks"
                }
                """
        ).flatMap(collStats -> {
            Number numBytesStorageUsed = collStats.get("storageSize", Number.class);
            DataSize current = DataSize.ofBytes(numBytesStorageUsed.longValue());
            DataSize max = vacuumingRequest.targetSize();

            double loadFactor = (double) current.toBytes() / (double) max.toBytes();
            LOGGER.info("Cache fill factor: {}% ({}/{}GB)", (int) (loadFactor * 100), current.toGigabytes(), max.toGigabytes());

            if (loadFactor < 1.) {
                LOGGER.info("No need for vacuuming");
                return Mono.just(new VacuumingResult(0L, DataSize.ofBytes(0L)));
            }

            return reactiveMongoTemplate
                .estimatedCount("fs.files")
                .doOnNext(totalFileCount -> LOGGER.info("Estimated file count is {}", totalFileCount))
                .map(totalFileCount -> (long) (totalFileCount - (totalFileCount / loadFactor)))
                .flatMap(toDeleteCount -> {
                    LOGGER.info("Vacuum will delete {} files", toDeleteCount);

                    return deleteRandomGridfsFiles(toIntExact(toDeleteCount))
                        .then(Mono.just(new VacuumingResult(toDeleteCount, DataSize.ofBytes(current.toBytes() - max.toBytes()))));
                });
        });
    }

    private String specToFilename(ImageSpec spec) {
        return String.join("/", spec.chapter(), spec.mode().name(), spec.file());
    }

    private Mono<Void> deleteRandomGridfsFiles(int n) {
        Query query = query(whereFilename().exists(true))
            .with(Sort.by(Order.asc("uploadDate")))
            .limit(n);
        return reactiveGridFsTemplate.delete(query);
    }

}
