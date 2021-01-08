package moe.tristan.kmdah.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class JdkHttpClientConfiguration {

    @Bean
    SimpleClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false); // ensures streaming mode
        requestFactory.setReadTimeout(5000); // give 5s to upstream to reply, or drop the connection altogether
        requestFactory.setOutputStreaming(true);
        return requestFactory;
    }

}
