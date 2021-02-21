package moe.tristan.kmdah.service.images.validation;

import java.io.IOException;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Base64.Decoder;

import org.apache.tuweni.crypto.sodium.SecretBox;
import org.apache.tuweni.crypto.sodium.SecretBox.Key;
import org.apache.tuweni.crypto.sodium.SecretBox.Nonce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import moe.tristan.kmdah.mangadex.image.ImageToken;
import moe.tristan.kmdah.service.gossip.messages.LeaderTokenEvent;

@Component
public class ImageRequestTokenValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageRequestTokenValidator.class);

    public static final Decoder B64_URL_DECODER = Base64.getUrlDecoder();
    public static final Decoder B64_NONURL_DECODER = Base64.getDecoder();

    private final Clock clock;
    private final ObjectMapper objectMapper;

    private String lastSecretKey = null;
    private Key secretKey = null;

    public ImageRequestTokenValidator(Clock clock, ObjectMapper objectMapper) {
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    public void validate(String token, String chapterHash) {
        if (secretKey == null) {
            LOGGER.warn("Secret key unset, not checking tokens.");
            return;
        }

        byte[] decryptedTokenBytes = decryptToken(token);

        ImageToken imageToken = openToken(decryptedTokenBytes);

        if (!imageToken.hash().equals(chapterHash)) {
            throw new InvalidImageRequestTokenException("Mismatched chapter hash! Requested chapter '" + chapterHash + "' but token was for '" + imageToken.hash() + "'");
        }

        ZonedDateTime now = ZonedDateTime.now(clock);
        if (now.isAfter(imageToken.expires())) {
            throw new InvalidImageRequestTokenException("Outdated token! Expires: " + imageToken.expires() + " is after now: " + now);
        }
    }

    @EventListener(LeaderTokenEvent.class)
    public void receivedTokenUpdateFromLeader(LeaderTokenEvent leaderTokenEvent) {
        if (lastSecretKey != null && lastSecretKey.equals(leaderTokenEvent.tokenKey())) {
            return; // we already have the latest and greatest :)
        }
        this.lastSecretKey = leaderTokenEvent.tokenKey();

        LOGGER.info("Received new secret key for token validation from leader: {}", leaderTokenEvent.tokenKey());
        byte[] secretKeyBytes = B64_NONURL_DECODER.decode(leaderTokenEvent.tokenKey());
        this.secretKey = Key.fromBytes(secretKeyBytes);
    }

    private byte[] decryptToken(String token) {
        byte[] tokenBytes = B64_URL_DECODER.decode(token);
        if (tokenBytes.length < 25) {
            throw new InvalidImageRequestTokenException("Token is invalid (expected length >= 25 bytes but was " + tokenBytes.length + " bytes): " + token);
        }

        int nonceLength = 24;
        byte[] nonceBytes = new byte[nonceLength];
        System.arraycopy(tokenBytes, 0, nonceBytes, 0, nonceLength);
        Nonce nonce = Nonce.fromBytes(nonceBytes);

        int cipherTextLength = tokenBytes.length - nonceLength;
        byte[] cipherText = new byte[cipherTextLength];
        System.arraycopy(tokenBytes, nonceLength, cipherText, 0, cipherTextLength);

        return SecretBox.decrypt(cipherText, secretKey, nonce);
    }

    private ImageToken openToken(byte[] decryptedTokenBytes) {
        try {
            return objectMapper.readValue(decryptedTokenBytes, ImageToken.class);
        } catch (IOException e) {
            throw new InvalidImageRequestTokenException("Invalid token payload!", e);
        }
    }

}
