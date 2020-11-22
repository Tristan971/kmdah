package moe.tristan.kmdah.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImageTokenValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageTokenValidator.class);

    void validate(String token, String chapter) {
        LOGGER.info("Not validating tokens.");
    }

}
