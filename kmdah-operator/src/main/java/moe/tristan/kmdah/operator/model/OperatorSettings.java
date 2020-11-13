package moe.tristan.kmdah.operator.model;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.operator")
public class OperatorSettings {

    private final int port;
    private final URI redirectUri;

    public OperatorSettings(int port, URI redirectUri) {
        this.port = port;
        this.redirectUri = redirectUri;
    }

    public int getPort() {
        return port;
    }

    public URI getRedirectUri() {
        return redirectUri;
    }

}
