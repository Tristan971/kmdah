package moe.tristan.kmdah.service.images.cache.mongodb;

import static java.util.Objects.requireNonNull;

import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Mono;

import com.mongodb.client.gridfs.model.GridFSFile;

import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;

@Component
public class MongodbCachedImageService implements CachedImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbCachedImageService.class);

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final ReactiveGridFsTemplate reactiveGridFsTemplate;

    public MongodbCachedImageService(
        ReactiveGridFsTemplate reactiveGridFsTemplate,
        ReactiveMongoTemplate reactiveMongoTemplate
    ) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.reactiveGridFsTemplate = reactiveGridFsTemplate;
    }

    @Override
    public Mono<ImageContent> findImage(ImageSpec imageSpec) {
        String filename = specToFilename(imageSpec);
        return reactiveGridFsTemplate
            .getResource(filename)
            .flatMap(resource -> resource.getGridFSFile().map(gridFsFile -> {
                OptionalLong contentLength = OptionalLong.of(gridFsFile.getLength());

                // parse metadata if exists
                Document metadata = gridFsFile.getMetadata();
                String mediaType = requireNonNull(metadata).getString(HttpHeaders.CONTENT_TYPE);

                return new ImageContent(
                    resource.getContent().share(),
                    MediaType.parseMediaType(mediaType),
                    contentLength,
                    CacheMode.HIT
                );
            }))
            .doOnNext(imageContent -> LOGGER.info("Retrieved {} from MongoDB~GridFS as {}", imageSpec, imageContent));
    }

    @Override
    public Mono<ObjectId> saveImage(ImageSpec imageSpec, ImageContent imageContent) {
        String filename = specToFilename(imageSpec);

        Document document = new Document();
        document.put(HttpHeaders.CONTENT_TYPE, imageContent.contentType().toString());
        LOGGER.info("Storing {} in MongoDB~GridFS as {} with metadata: {}", imageSpec, filename, document.toString());

        return reactiveGridFsTemplate
            .store(imageContent.bytes(), filename, document)
            .doOnSuccess(objectId -> LOGGER.info(
                "Stored {} in MongoDB~GridFS as _id:{}/{} with metadata: {}",
                imageSpec,
                objectId,
                filename,
                document.toString()
            ))
            .doOnError(err -> LOGGER.error(
                "Failed storing {} in MongoFB~GridFS as {} with metadata: {}",
                imageSpec,
                filename,
                document.toString(),
                err
            ));
    }

    @Override
    public Mono<VacuumingResult> vacuum(VacuumingRequest vacuumingRequest) {
        return reactiveMongoTemplate
            .count(Query.query(Criteria.where("_id").exists(true)), "fs.chunks")
            .flatMap(count -> {
                DataSize estimatedSize = DataSize.ofKilobytes(count * 255);
                return doVacuum(estimatedSize, vacuumingRequest.targetSize());
            });
    }

    private String specToFilename(ImageSpec spec) {
        return String.join("/", spec.chapter(), spec.mode().name(), spec.file());
    }

    private Mono<VacuumingResult> doVacuum(DataSize current, DataSize max) {
        double loadFactor = (double) current.toBytes() / (double) max.toBytes();
        LOGGER.info("Cache fill factor: {}% ({}/{}GB)", (int) (loadFactor * 100), current.toGigabytes(), max.toGigabytes());

        if (loadFactor < 1.) {
            LOGGER.info("No need for vacuuming");
            return Mono.just(new VacuumingResult(0L, DataSize.ofBytes(0L)));
        }

        return reactiveMongoTemplate
            .count(Query.query(Criteria.where("_id").exists(true)), "fs.files")
            .doOnNext(totalFileCount -> LOGGER.info("Total file count is {}", totalFileCount))
            .map(totalFileCount -> (long) (totalFileCount - (totalFileCount / loadFactor)))
            .doOnNext(toDeleteCount -> LOGGER.info("Will attempt deleting {} files", toDeleteCount))
            .flatMap(toDeleteCount -> reactiveGridFsTemplate
                .getResources("**")
                .limitRequest(toDeleteCount)
                .flatMap(ReactiveGridFsResource::getGridFSFile)
                .collectList()
            ).doOnNext(filesToDelete -> LOGGER.info("Collected {} files for deletion", filesToDelete.size()))
            .flatMap(filesToDelete -> {
                Set<BsonValue> toDeleteIds = filesToDelete.stream().map(GridFSFile::getId).collect(Collectors.toSet());
                return reactiveGridFsTemplate
                    .delete(Query.query(Criteria.where("_id").in(toDeleteIds)))
                    .doOnSuccess(__ -> LOGGER.info("Successfully deleted files!"))
                    .thenReturn(new VacuumingResult(
                        filesToDelete.size(),
                        DataSize.ofBytes(filesToDelete.stream().map(GridFSFile::getLength).mapToLong(Long::longValue).sum())
                    ));
            });
    }

}
