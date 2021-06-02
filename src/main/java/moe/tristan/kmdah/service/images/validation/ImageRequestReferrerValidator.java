package moe.tristan.kmdah.service.images.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class ImageRequestReferrerValidator {

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
                throw new InvalidImageRequestReferrerException("Invalid referrer didn't have a host for " + referrer);
            }

            if (!VALID_REFERERS.matcher(host).find()) {
                throw new InvalidImageRequestReferrerException("Invalid Referrer header had unexpected host for " + referrer);
            }
        } catch (URISyntaxException e) {
            throw new InvalidImageRequestReferrerException("Invalid Referrer header was present but not a URI for " + referrer, e);
        }
    }

}
