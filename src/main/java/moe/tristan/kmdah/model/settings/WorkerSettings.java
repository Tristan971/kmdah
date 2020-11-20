package moe.tristan.kmdah.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.worker")
public record WorkerSettings(
    int port,
    int bandwidthMbps
) {}
