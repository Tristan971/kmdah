package moe.tristan.kmdah.operator.service.kubernetes;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

@Configuration
public class KubernetesClientConfiguration {

    @Bean
    public ApiClient kubernetesClient() throws IOException {
        return Config.defaultClient();
    }

    @Bean
    public CoreV1Api kubernetesCoreV1Api(ApiClient kubernetesClient) {
        return new CoreV1Api(kubernetesClient);
    }

}
