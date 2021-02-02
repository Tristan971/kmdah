package moe.tristan.kmdah.service.images.validation;

import static moe.tristan.kmdah.mangadex.image.ImageMode.DATA;

import java.util.OptionalLong;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.metrics.ImageMetrics;

@Component
public class ImageValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageValidationService.class);

    private final ImageMetrics imageMetrics;

    public ImageValidationService(ImageMetrics imageMetrics) {
        this.imageMetrics = imageMetrics;
    }

    public boolean validate(ImageSpec imageSpec, OptionalLong expectedLength, byte[] bytes) {
        if (expectedLength.isPresent()) {
            if (expectedLength.getAsLong() != bytes.length) {
                LOGGER.error("Mismatched length for {} ; expected {} bytes but got {} bytes", imageSpec, expectedLength.getAsLong(), bytes.length);
                imageMetrics.recordValidation(false);
                return false;
            } else {
                LOGGER.info("Byte count match");
                imageMetrics.recordValidation(true);
                return true;
            }
        }

        if (DATA.equals(imageSpec.mode()) && imageSpec.file().contains("-")) {
            String expectedShasum = imageSpec.file().split("-")[1].split("\\.")[0];
            String actualShasum = DigestUtils.sha256Hex(bytes);
            if (!expectedShasum.equals(actualShasum)) {
                LOGGER.error("Mismatched shasum for {} ; expected [{}] but got [{}]", imageSpec, expectedShasum, actualShasum);
                imageMetrics.recordValidation(false);
                return false;
            } else {
                LOGGER.info("Shasum match: {}", actualShasum);
            }
        }

        imageMetrics.recordValidation(true);
        return true;
    }

}
