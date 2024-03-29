package moe.tristan.kmdah;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.module.blackbird.BlackbirdModule;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    ClientHttpConnector clientHttpConnector() {
        return new HttpComponentsClientHttpConnector();
    }

    @Configuration
    @ConditionalOnProperty(name = "kmdah.experimental-features.jackson-blackbird", havingValue = "true")
    public static class JacksonBlackbirdConfiguration {

        @Bean
        public BlackbirdModule blackbirdModule() {
            return new BlackbirdModule();
        }

    }

}
