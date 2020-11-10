package moe.tristan.kmdah.common;

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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import moe.tristan.kmdah.common.model.settings.CacheSettings;

@Configuration
@EntityScan
@ComponentScan
@ConfigurationPropertiesScan
public class KmdahCommonConfiguration {

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
    public AmazonS3 s3cacheClient(CacheSettings cacheSettings) {
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(
            cacheSettings.getS3Auth().getAccessKeyId(),
            cacheSettings.getS3Auth().getSecretAccessKey()
        ));

        EndpointConfiguration endpointConfiguration = new EndpointConfiguration(
            cacheSettings.getS3Auth().getServiceUri(),
            cacheSettings.getS3Auth().getSigningRegion()
        );

        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(credentialsProvider)
            .withEndpointConfiguration(endpointConfiguration)
            .withPathStyleAccessEnabled(true)
            .build();
    }

}
