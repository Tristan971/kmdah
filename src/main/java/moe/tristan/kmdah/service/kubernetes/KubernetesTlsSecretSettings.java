package moe.tristan.kmdah.service.kubernetes;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.k8s.tls-secret")
public record KubernetesTlsSecretSettings(

    String name,

    String namespace,

    boolean autoUpdate

) {}
