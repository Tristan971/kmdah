package moe.tristan.kmdah.service.kubernetes;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

@Configuration
public class KubernetesConfiguration {

    @Bean
    public CoreV1Api kubernetesCoreV1Api() throws IOException {
        return new CoreV1Api(Config.defaultClient());
    }

}
