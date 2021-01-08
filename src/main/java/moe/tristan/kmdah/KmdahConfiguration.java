package moe.tristan.kmdah;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor(
            Thread
                .builder()
                .virtual()
                .name("virt-", 0)
                .factory()
        );
    }

}
