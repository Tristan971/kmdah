package moe.tristan.kmdah;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;

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
    public LettuceClientConfigurationBuilderCustomizer customizer(@Value("${spring.application.name}") String instanceName) {
        return clientConfigurationBuilder -> {
            // manually specifying RESP3 (Redis 6+) since this can't be autoresolved against a password-protected instance
            clientConfigurationBuilder.clientName(instanceName);
            clientConfigurationBuilder.clientOptions(
                ClientOptions
                    .builder()
                    .autoReconnect(true)
                    .protocolVersion(ProtocolVersion.RESP3)
                    .build()
            );
        };
    }

}
