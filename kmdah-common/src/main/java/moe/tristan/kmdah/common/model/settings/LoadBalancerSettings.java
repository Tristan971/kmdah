package moe.tristan.kmdah.common.model.settings;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.load-balancer")
public class LoadBalancerSettings {

    private final URI uri;

    public LoadBalancerSettings(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

}
