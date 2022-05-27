package moe.tristan.kmdah.service.tls.k8s;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Valid
@ConfigurationProperties("kmdah.tls.k8s.secret")
public record K8sTlsSecretSettings(

    @NotBlank
    String name,

    @NotBlank
    String namespace,

    boolean autoUpdate

) { }
