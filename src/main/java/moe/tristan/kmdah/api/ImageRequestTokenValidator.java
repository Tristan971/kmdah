package moe.tristan.kmdah.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImageRequestTokenValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageRequestTokenValidator.class);

    void validate(String token, String chapter) {
        LOGGER.info("Not validating tokens.");
    }

}
