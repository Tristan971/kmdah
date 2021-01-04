package moe.tristan.kmdah.service.images.cache.filesystem;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("filesystem")
@EnableConfigurationProperties(FilesystemSettings.class)
public class FilesystemConfiguration {

    @Bean
    FilesystemCachedImageService filesystemCachedImageService(FilesystemSettings filesystemSettings) {
        return new FilesystemCachedImageService(filesystemSettings);
    }

}
