package moe.tristan.kmdah.service.images.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ImageRequestReferrerValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageRequestReferrerValidator.class);

    private static final Pattern VALID_REFERERS = Pattern.compile(
        // *.*.mangadex.network
        // mangadex.(cc/org)(.*.mdcloud.moe)
        "^(https?://)?((.+\\..+\\.)?mangadex\\.(cc|org|network))(\\..+\\.mdcloud\\.moe)?$"
    );

    public void validate(String referrer) {
        if (referrer == null || "".equals(referrer)) {
            return;
        }

        try {
            URI referrerUri = new URI(referrer);
            String host = referrerUri.getHost();
            if (host == null) {
                LOGGER.warn("Invalid referrer: {}", referrer);
                return;
                //throw new InvalidImageRequestReferrerException("Invalid referrer didn't have a host for " + referrer);
            }

            if (!VALID_REFERERS.matcher(host).find()) {
                LOGGER.warn("Illegal referrer: {}", referrer);
                //throw new InvalidImageRequestReferrerException("Invalid Referrer header had unexpected host for " + referrer);
            }
        } catch (URISyntaxException e) {
            LOGGER.warn("Invalid referrer isn't a URI: {}", referrer);
            throw new InvalidImageRequestReferrerException("Invalid Referrer header was present but not a URI for " + referrer, e);
        }
    }

}
