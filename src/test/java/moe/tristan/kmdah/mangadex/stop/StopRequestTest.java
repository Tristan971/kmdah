package moe.tristan.kmdah.mangadex.stop;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = JacksonAutoConfiguration.class)
class StopRequestTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializes() throws JsonProcessingException, JSONException {
        String expected = """
            {
              "secret": "secret"
            }
            """;

        StopRequest request = new StopRequest("secret");
        String actual = objectMapper.writeValueAsString(request);

        JSONAssert.assertEquals(expected, actual, true);
    }

}
