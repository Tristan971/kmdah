package moe.tristan.kmdah.mangadex.ping;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = JacksonAutoConfiguration.class)
class PingResponseTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void withTls() throws JsonProcessingException {
        String response = """
            {
              "image_server": "https://s2.mangadex.org",
              "latest_build": "121",
              "url": "https://clienturl.mangadex.network",
              "token_key": "abcd1234",
              "compromised": false,
              "paused": false,
              "tls": {
                "created_at": "1996-01-10T12:00:00.000000000Z",
                "private_key": "privkey",
                "certificate": "certificate"
              }
            }
            """;

        PingResponse expected = new PingResponse(
            "https://s2.mangadex.org",
            "121",
            "https://clienturl.mangadex.network",
            "abcd1234",
            false,
            false,
            Optional.of(new TlsData(
                LocalDateTime.of(
                    LocalDate.of(1996, 1, 10),
                    LocalTime.NOON
                ),
                "privkey",
                "certificate"
            ))
        );

        PingResponse actual = objectMapper.readValue(response, PingResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void withoutTls() throws JsonProcessingException {
        String response = """
            {
              "image_server": "https://s2.mangadex.org",
              "latest_build": "121",
              "url": "https://clienturl.mangadex.network",
              "token_key": "abcd1234",
              "compromised": false,
              "paused": false
            }
            """;

        PingResponse expected = new PingResponse(
            "https://s2.mangadex.org",
            "121",
            "https://clienturl.mangadex.network",
            "abcd1234",
            false,
            false,
            Optional.empty()
        );

        PingResponse actual = objectMapper.readValue(response, PingResponse.class);
        assertThat(actual).isEqualTo(expected);
    }

}
