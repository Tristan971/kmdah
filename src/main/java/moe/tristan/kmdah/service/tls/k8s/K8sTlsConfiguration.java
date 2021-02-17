package moe.tristan.kmdah.service.tls.k8s;

import java.io.IOException;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

@Configuration
@Profile("tls-k8s")
@EnableConfigurationProperties(K8sTlsSecretSettings.class)
public class K8sTlsConfiguration {

    @Bean
    public CoreV1Api kubernetesCoreV1Api() throws IOException {
        return new CoreV1Api(Config.defaultClient());
    }

    @Bean
    public K8sTlsConfigurationService kubernetesTlsConfigurationService(
        CoreV1Api coreV1Api,
        K8sTlsSecretSettings kubernetesTlsSecretSettings
    ) {
        return new K8sTlsConfigurationService(kubernetesTlsSecretSettings, coreV1Api);
    }

}
