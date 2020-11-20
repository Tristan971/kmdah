package moe.tristan.kmdah.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.web")
public record NetworkSettings(
    int port,
    int bandwidthMbps,
    int gracefulShutdownSeconds
) {}
