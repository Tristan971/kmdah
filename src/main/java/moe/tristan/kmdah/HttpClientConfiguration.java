package moe.tristan.kmdah;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    SimpleClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false); // ensures streaming mode
        requestFactory.setReadTimeout(5000); // give 5s to upstream to reply, or drop the connection altogether
        requestFactory.setConnectTimeout(5000); // give 5s to upstream to accept connection
        requestFactory.setOutputStreaming(true);
        return requestFactory;
    }

}
