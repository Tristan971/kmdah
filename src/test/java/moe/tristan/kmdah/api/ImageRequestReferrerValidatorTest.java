package moe.tristan.kmdah.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ImageRequestReferrerValidatorTest {

    private final ImageRequestReferrerValidator validator = new ImageRequestReferrerValidator();

    @Test
    void testValid() {
        validator.validate("https://mangadex.org");
        validator.validate("https://mangadex.network");
        validator.validate("https://mdah.tristan.moe");
        validator.validate("");
        validator.validate(null);
    }

    @Test
    void testInvalid() {
        assertThatThrownBy(() -> validator.validate("https://notmangadex.org"))
            .isInstanceOf(InvalidImageRequestReferrerException.class)
            .hasMessageContaining("https://notmangadex.org");

        assertThatThrownBy(() -> validator.validate("not-a-uri"))
            .isInstanceOf(InvalidImageRequestReferrerException.class)
            .hasMessageContaining("Invalid referrer didn't have a host");
    }

}
