package moe.tristan.kmdah.service.tls.file;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kmdah.tls.file")
public record FileTlsConfigurationSettings(
    Path certificateOutputFile,
    Path privateKeyOutputFile
) {}
