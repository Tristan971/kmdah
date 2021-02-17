package moe.tristan.kmdah.service.tls.k8s;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.tls.k8s.secret")
public record K8sTlsSecretSettings(

    String name,

    String namespace,

    boolean autoUpdate

) {}
