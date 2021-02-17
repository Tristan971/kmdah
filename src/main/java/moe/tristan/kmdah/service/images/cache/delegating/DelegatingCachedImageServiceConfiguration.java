package moe.tristan.kmdah.service.images.cache.delegating;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import moe.tristan.kmdah.service.images.cache.CachedImageService;

@Configuration
@Profile("cache-delegating")
public class DelegatingCachedImageServiceConfiguration {

    @Bean
    public CachedImageService delegatingCachedImageService() {
        return new DelegatingCachedImageService();
    }

}
