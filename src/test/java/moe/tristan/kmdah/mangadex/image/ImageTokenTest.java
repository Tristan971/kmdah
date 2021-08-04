package moe.tristan.kmdah.mangadex.image;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = JacksonAutoConfiguration.class)
class ImageTokenTest {

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @ValueSource(strings = {
        // 8 digits of precision
        """
            {
                "expires":"2021-02-21T03:12:34.94241684Z",
                "hash":"d7185218334f62211fec52c64357fd14"
            }
            """,
        // 9 digits of precision
        """
            {
                "expires":"2021-02-21T03:12:34.942416840Z",
                "hash":"d7185218334f62211fec52c64357fd14"
            }
            """
    })
    void variousSamples(String payload) throws IOException {
        objectMapper.readValue(payload.getBytes(StandardCharsets.UTF_8), ImageToken.class);
    }

}
