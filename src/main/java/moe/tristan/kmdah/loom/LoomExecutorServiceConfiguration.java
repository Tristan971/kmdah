package moe.tristan.kmdah.loom;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoomExecutorServiceConfiguration {

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
