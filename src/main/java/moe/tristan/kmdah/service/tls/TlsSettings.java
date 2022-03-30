package moe.tristan.kmdah.service.tls;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kmdah.tls")
public record TlsSettings(
    TlsBackend backend
) {}
