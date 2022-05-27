package moe.tristan.kmdah.service.tls;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Valid
@ConfigurationProperties("kmdah.tls")
public record TlsSettings(

    @NotNull
    TlsBackend backend

) { }
