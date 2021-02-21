package moe.tristan.kmdah.service.images.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;

import moe.tristan.kmdah.KmdahConfiguration;
import moe.tristan.kmdah.service.gossip.messages.LeaderTokenEvent;

@SpringBootTest(classes = {
    ImageRequestTokenValidator.class,
    JacksonAutoConfiguration.class,
    ImageRequestTokenValidatorTest.ClockConfiguration.class
})
class ImageRequestTokenValidatorTest {

    private static final String SECRET_KEY = "jRrpGgZ7jNLEYbaUuToxfIhNMdQr4wL41nOQjTOWQr4=";
    private static final String TOKEN = "mAVIDb9zAiqX53RE5zDxwuCF2XtyB9zV9VLMwTrXyJLeBhOCWpRgpOe-cWOsmDnIJU6K2IrcIXl7-CanizbO2AAsIpyX-f9u1k64IXm8XY8gPLaY15GVVLFDZMiHgJ-pW1eVFpL9k9MRRE6DFhuwZLWdH-hUoJGTVvRRItDFYU7vo4pOc_WQTECvaD0-uaTVwslpolpCid7IwzE52RsMSsHScp7T";
    private static final long TOKEN_EPIRES = 1613867356L;
    private static final String TOKEN_HASH = "cae036bff6074695c9629bdc1ba9d6ca";

    @Autowired
    private ImageRequestTokenValidator tokenValidator;

    @SpyBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        tokenValidator.receivedTokenUpdateFromLeader(new LeaderTokenEvent(SECRET_KEY));
    }

    @Test
    void failsOnBadTokenSyntax() {
        String sampleToken = "abc";
        assertThatThrownBy(() -> tokenValidator.validate(sampleToken, "test"))
            .isInstanceOf(InvalidImageRequestTokenException.class)
            .hasMessageContaining(sampleToken);
    }

    @Test
    void failsOnMismatchedChapter() {
        withCurrentTimeEpochSecond(TOKEN_EPIRES - 10);
        assertThatThrownBy(() -> tokenValidator.validate(TOKEN, "not the chapter for this token!"))
            .isInstanceOf(InvalidImageRequestTokenException.class)
            .hasMessageContaining("Mismatched chapter hash");
    }

    @Test
    void failsOnOutdatedToken() {
        withCurrentTimeEpochSecond(TOKEN_EPIRES + 10);
        assertThatThrownBy(() -> tokenValidator.validate(TOKEN, TOKEN_HASH))
            .isInstanceOf(InvalidImageRequestTokenException.class)
            .hasMessageContaining("Outdated token");
    }

    @Test
    void succeedsOnValidToken() {
        withCurrentTimeEpochSecond(TOKEN_EPIRES - 10);
        tokenValidator.validate(TOKEN, TOKEN_HASH);
    }


    private void withCurrentTimeEpochSecond(long epochSecond) {
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(epochSecond));
    }

    @TestConfiguration
    public static class ClockConfiguration {

        @Bean
        public Clock clock() {
            return new KmdahConfiguration().utcClock();
        }

    }

}
