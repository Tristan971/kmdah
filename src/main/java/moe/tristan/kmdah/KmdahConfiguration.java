package moe.tristan.kmdah;

import java.io.IOException;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
@EntityScan
@ComponentScan
@ConfigurationPropertiesScan
public class KmdahConfiguration {

    @Bean
    @Primary
    public Clock utcClock() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        return Clock.systemUTC();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }

    @Bean
    public ApiClient kubernetesClient() throws IOException {
        return Config.defaultClient();
    }

    @Bean
    public CoreV1Api kubernetesCoreV1Api(ApiClient kubernetesClient) {
        return new CoreV1Api(kubernetesClient);
    }

}
