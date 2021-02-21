package moe.tristan.kmdah.service.images.validation;

import java.io.IOException;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Base64.Decoder;

import org.apache.tuweni.bytes.Bytes;
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

    private ZonedDateTime lastAnnounce;

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

        if (lastAnnounce == null) {
            LOGGER.warn("Time handling not ready, not checking tokens.");
            return;
        }

        byte[] decryptedTokenBytes = decryptToken(token);

        ImageToken imageToken = openToken(decryptedTokenBytes);

        if (!imageToken.hash().equals(chapterHash)) {
            throw new InvalidImageRequestTokenException("Mismatched chapter hash! Requested chapter '" + chapterHash + "' but token was for '" + imageToken.hash() + "'");
        }

        if (lastAnnounce.isAfter(imageToken.expires())) {
            throw new InvalidImageRequestTokenException("Outdated token! Expires: " + imageToken.expires() + " is after now: " + lastAnnounce);
        }
    }

    @EventListener(LeaderTokenEvent.class)
    public void receivedTokenUpdateFromLeader(LeaderTokenEvent leaderTokenEvent) {
        this.lastAnnounce = ZonedDateTime.now(clock);
        LOGGER.info("New token comparison time: {}", lastAnnounce);

        if (lastSecretKey != null && lastSecretKey.equals(leaderTokenEvent.tokenKey())) {
            return; // we already have the latest and greatest :)
        }
        this.lastSecretKey = leaderTokenEvent.tokenKey();

        LOGGER.info("Received new secret key for token validation from leader: {}", leaderTokenEvent.tokenKey());
        byte[] secretKeyBytes = B64_NONURL_DECODER.decode(leaderTokenEvent.tokenKey());

        Key newKey = Key.fromBytes(secretKeyBytes);
        if (this.secretKey != null && !this.secretKey.isDestroyed()) {
            this.secretKey.destroy();
        }
        this.secretKey = newKey;
    }

    private byte[] decryptToken(String token) {
        byte[] tokenBytes = B64_URL_DECODER.decode(token);
        if (tokenBytes.length < 25) {
            throw new InvalidImageRequestTokenException("Token is invalid (expected length >= 25 bytes but was " + tokenBytes.length + " bytes): " + token);
        }

        Nonce nonce = null;
        try {
            int nonceLength = 24;
            nonce = Nonce.fromBytes(Bytes.wrap(tokenBytes, 0, nonceLength));
            int cipherTextLength = tokenBytes.length - nonceLength;
            Bytes cipherText = Bytes.wrap(tokenBytes, nonceLength, cipherTextLength);
            //noinspection ConstantConditions
            return SecretBox.decrypt(cipherText, secretKey, nonce).toArrayUnsafe();
        } finally {
            if (nonce != null && !nonce.isDestroyed()) {
                nonce.destroy();
            }
        }
    }

    private ImageToken openToken(byte[] decryptedTokenBytes) {
        try {
            return objectMapper.readValue(decryptedTokenBytes, ImageToken.class);
        } catch (IOException e) {
            throw new InvalidImageRequestTokenException("Invalid token payload!", e);
        }
    }

}
