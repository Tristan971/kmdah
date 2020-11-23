package moe.tristan.kmdah.mangadex.ping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = JacksonAutoConfiguration.class)
class PingRequestTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void withoutTlsCreatedAt() throws JsonProcessingException, JSONException {
        PingRequest request = new PingRequest(
            "secret",
            443,
            1234L,
            4567L,
            Optional.empty(),
            19
        );
        String actual = objectMapper.writeValueAsString(request);

        String expected = """
            {
              "secret": "secret",
              "port": 443,
              "disk_space": 1234,
              "network_speed": 4567,
              "build_version": 19
            }
            """;

        JSONAssert.assertEquals(expected, actual, true);
    }

    @Test
    void withTlsCreatedAt() throws JsonProcessingException, JSONException {
        ZonedDateTime lastCreatedAt = ZonedDateTime.of(
            LocalDate.of(1996, 1, 10),
            LocalTime.NOON,
            ZoneOffset.UTC
        );

        PingRequest request = new PingRequest(
            "secret",
            443,
            1234L,
            4567L,
            Optional.of(lastCreatedAt),
            19
        );
        String actual = objectMapper.writeValueAsString(request);

        String expected = """
            {
              "secret": "secret",
              "port": 443,
              "disk_space": 1234,
              "network_speed": 4567,
              "tls_created_at": "1996-01-10T12:00:00.000000000Z",
              "build_version": 19
            }
            """;

        JSONAssert.assertEquals(expected, actual, true);
    }

}
