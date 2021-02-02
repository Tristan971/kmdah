package moe.tristan.kmdah;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.whereFilename;

import java.io.IOException;
import java.util.OptionalLong;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.mongodb.client.gridfs.model.GridFSFile;

import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.validation.ImageValidationService;
import moe.tristan.kmdah.util.ThrottledExecutorService;

@Component
public class CachePruning implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachePruning.class);

    private static final Pattern DATA_SHASUMD_PAT = Pattern.compile("^.+/DATA/.+-.+");
    private static final ExecutorService EXECUTOR_SERVICE = ThrottledExecutorService.from(1, 1, 1);

    private final GridFsTemplate gridFsTemplate;
    private final ImageValidationService imageValidationService;

    public CachePruning(GridFsTemplate gridFsTemplate, ImageValidationService imageValidationService) {
        this.gridFsTemplate = gridFsTemplate;
        this.imageValidationService = imageValidationService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.getNonOptionArgs().contains("cacheScanning")) {
            LOGGER.info("Cache scanning not requested!");
            return;
        }

        gridFsTemplate.find(
            query(whereFilename().regex(DATA_SHASUMD_PAT))
                .with(Sort.by(Sort.Order.desc("uploadDate")))
        ).forEach(file -> {
            try {
                EXECUTOR_SERVICE.submit(() -> doValidate(file)).get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Failed run for {}", file.getFilename(), e);
            }
        });
    }

    private void doValidate(GridFSFile file) {
        try {
            String[] parts = file.getFilename().split("/");
            ImageSpec spec = new ImageSpec(ImageMode.valueOf(parts[1]), parts[0], parts[2]);
            LOGGER.info("Validating [{}] {}", file.getUploadDate(), spec);

            byte[] bytes = StreamUtils.copyToByteArray(gridFsTemplate.getResource(file).getInputStream());
            if (!imageValidationService.validate(spec, OptionalLong.empty(), bytes)) {
                LOGGER.warn("Invalid");
                gridFsTemplate.delete(query(whereFilename().is(file.getFilename())));
                LOGGER.warn("Deleted");
            } else {
                LOGGER.info("Validated file {}", file.getFilename());
            }
        } catch (IOException e) {
            LOGGER.error("Can't validate file {}", file.getFilename(), e);
        }
    }

}
