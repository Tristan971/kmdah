package moe.tristan.kmdah.service.tls;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.tls")
public record TlsSettings(
    TlsBackend backend
) {}
