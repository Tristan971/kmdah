package moe.tristan.kmdah.service.images.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImageRequestTokenValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageRequestTokenValidator.class);

    public void validate(String token, String chapter) {
        LOGGER.debug("Not validating tokens.");
    }

}
