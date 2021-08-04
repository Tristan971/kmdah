package moe.tristan.kmdah;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    SimpleClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(5000); // give 5s to upstream to reply, or drop the connection altogether
        requestFactory.setConnectTimeout(5000); // give 5s to upstream to accept connection
        requestFactory.setBufferRequestBody(false); // ensures streaming mode
        requestFactory.setOutputStreaming(true);
        return requestFactory;
    }

    @Bean
    public BlackbirdModule blackbirdModule() {
        return new BlackbirdModule();
    }

}
