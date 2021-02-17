package moe.tristan.kmdah.service.tls.file;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.tls.file")
public record FileTlsConfigurationSettings(
    Path certificateOutputFile,
    Path privateKeyOutputFile
) {}
