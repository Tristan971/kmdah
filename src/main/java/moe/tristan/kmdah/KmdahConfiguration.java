package moe.tristan.kmdah;

import java.io.IOException;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

@Configuration
@ConfigurationPropertiesScan
public class KmdahConfiguration {

    @Bean
    @Primary
    public Clock utcClock() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        return Clock.systemUTC();
    }

    @Bean
    public CoreV1Api kubernetesCoreV1Api() throws IOException {
        return new CoreV1Api(Config.defaultClient());
    }

}
