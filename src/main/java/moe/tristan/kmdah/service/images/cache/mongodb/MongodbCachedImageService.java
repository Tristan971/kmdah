package moe.tristan.kmdah.service.images.cache.mongodb;

import static java.lang.Math.toIntExact;
import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.whereFilename;

import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;

import com.mongodb.client.gridfs.model.GridFSFile;

import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;
import moe.tristan.kmdah.service.images.cache.VacuumingResult.VacuumGranularity;

public class MongodbCachedImageService implements CachedImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbCachedImageService.class);

    private final MongoTemplate mongoTemplate;
    private final GridFsTemplate gridFsTemplate;

    public MongodbCachedImageService(MongoTemplate mongoTemplate, GridFsTemplate gridFsTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.gridFsTemplate = gridFsTemplate;
    }

    @Override
    public Optional<ImageContent> findImage(ImageSpec imageSpec) {
        String filename = specToFilename(imageSpec);

        GridFSFile gridFsFile = gridFsTemplate.find(query(whereFilename().is(filename)).limit(1)).first();

        if (gridFsFile == null) {
            return Optional.empty();
        }

        OptionalLong contentLength = OptionalLong.of(gridFsFile.getLength());

        String mediaType = requireNonNull(gridFsFile.getMetadata()).getString("_contentType");
        Instant lastModified = gridFsFile.getUploadDate().toInstant();

        return Optional.of(new ImageContent(
            gridFsTemplate.getResource(gridFsFile),
            MediaType.parseMediaType(mediaType),
            contentLength,
            lastModified,
            CacheMode.HIT
        ));
    }

    @Override
    public void saveImage(ImageSpec imageSpec, MediaType mediaType, InputStream inputStream) {
        String filename = specToFilename(imageSpec);

        LOGGER.debug("Storing {} in MongoDB~GridFS as {} with content type: {}", imageSpec, filename, mediaType);

        try {
            ObjectId storedObject = gridFsTemplate.store(
                inputStream,
                filename,
                mediaType.toString()
            );

            LOGGER.debug(
                "Stored {} in MongoDB~GridFS as _id:{}/{} with content type: {}",
                imageSpec,
                storedObject,
                filename,
                mediaType
            );
        } catch (Exception e) {
            LOGGER.error("Failed storing {} in mongodb!", imageSpec, e);
            gridFsTemplate.delete(query(whereFilename().is(filename)));
        }

    }

    @Override
    public void deleteChapter(ImageSpec imageSpec) {
        String filename = specToFilename(imageSpec); // for chapters, the spec has * as filename so will match the whole chapter
        for (GridFsResource resource : gridFsTemplate.getResources(filename)) {
            LOGGER.info("Deleting {}", resource.getFilename());
            gridFsTemplate.delete(query(whereFilename().is(resource.getFilename())));
        }
    }

    @Override
    public VacuumingResult vacuum(VacuumingRequest vacuumingRequest) {
        Document collStats = mongoTemplate.executeCommand(
            """
                {
                    "collStats": "fs.chunks"
                }
                """
        );

        Number numBytesStorageAllocated = collStats.get("totalSize", Number.class);

        long usedBytesCount = numBytesStorageAllocated.longValue();
        if (collStats.containsKey("freeStorageSize")) {
            Number numBytesReusableStorage = collStats.get("freeStorageSize", Number.class);
            long reusableBytesCount = numBytesReusableStorage.longValue();

            LOGGER.info(
                "MongoDB has allocated a total of {}GB on disk, but {}GB are unused and will be automatically reused.",
                DataSize.ofBytes(usedBytesCount).toGigabytes(),
                DataSize.ofBytes(reusableBytesCount).toGigabytes()
            );
            usedBytesCount -= reusableBytesCount;
        }

        DataSize current = DataSize.ofBytes(usedBytesCount);
        DataSize max = vacuumingRequest.targetSize();

        double loadFactor = (double) current.toBytes() / (double) max.toBytes();
        LOGGER.info("Cache fill factor: {}% ({}/{}GB)", (int) (loadFactor * 100), current.toGigabytes(), max.toGigabytes());

        if (loadFactor < 1.) {
            LOGGER.info("No need for vacuuming");
            return new VacuumingResult(0L, DataSize.ofBytes(0L), VacuumGranularity.FILE);
        }

        long totalFileCount = mongoTemplate.estimatedCount("fs.files");
        LOGGER.info("Estimated file count is {}", totalFileCount);

        long toDeleteCount = (long) (totalFileCount - (totalFileCount / loadFactor));
        LOGGER.info("Vacuum will delete {} files", toDeleteCount);

        deleteRandomGridfsFiles(toIntExact(toDeleteCount));

        return new VacuumingResult(toDeleteCount, DataSize.ofBytes(current.toBytes() - max.toBytes()), VacuumGranularity.FILE);
    }

    private String specToFilename(ImageSpec spec) {
        return String.join("/", spec.chapter(), spec.mode().name(), spec.file());
    }

    private void deleteRandomGridfsFiles(int n) {
        Query query = query(whereFilename().exists(true))
            .with(Sort.by(Order.asc("uploadDate")))
            .limit(n);
        gridFsTemplate.delete(query);
    }

}
