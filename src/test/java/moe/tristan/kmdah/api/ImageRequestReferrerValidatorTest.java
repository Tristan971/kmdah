package moe.tristan.kmdah.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import moe.tristan.kmdah.service.images.validation.ImageRequestReferrerValidator;
import moe.tristan.kmdah.service.images.validation.InvalidImageRequestReferrerException;

class ImageRequestReferrerValidatorTest {

    private final ImageRequestReferrerValidator validator = new ImageRequestReferrerValidator();

    @Test
    void testValid() {
        validator.validate("https://mangadex.org");
        validator.validate("http://mangadex.org");
        validator.validate("https://mangadex.cc");
        validator.validate("http://mangadex.cc");
        validator.validate("https://abcd.efgh.mangadex.network");
        validator.validate("https://mangadex.org.dev.mdcloud.moe");
        validator.validate("");
        validator.validate(null);
    }

    @Test
    @Disabled
    void testInvalid() {
        assertThatThrownBy(() -> validator.validate("https://notmangadex.org"))
            .isInstanceOf(InvalidImageRequestReferrerException.class)
            .hasMessageContaining("https://notmangadex.org");

        assertThatThrownBy(() -> validator.validate("not-a-uri"))
            .isInstanceOf(InvalidImageRequestReferrerException.class)
            .hasMessageContaining("Invalid referrer didn't have a host");
    }

}
