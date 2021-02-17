package moe.tristan.kmdah.service.tls.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import moe.tristan.kmdah.service.tls.TlsConfigurationService;

@Configuration
@Profile("tls-file")
@EnableConfigurationProperties(FileTlsConfigurationSettings.class)
public class FileTlsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTlsConfiguration.class);

    FileTlsConfiguration(FileTlsConfigurationSettings fileTlsConfigurationSettings) {
        LOGGER.info("Using file backend for TLS configuration. Configuration: {}", fileTlsConfigurationSettings);
    }

    @Bean
    public TlsConfigurationService fileTlsConfigurationService(FileTlsConfigurationSettings fileTlsConfigurationSettings) {
        return new FileTlsConfigurationService(fileTlsConfigurationSettings);
    }

}
