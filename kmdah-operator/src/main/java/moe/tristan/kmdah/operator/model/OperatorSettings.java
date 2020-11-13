package moe.tristan.kmdah.operator.model;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.operator")
public class OperatorSettings {

    private final int port;
    private final int pingFrequencySeconds;
    private final URI redirectUri;

    public OperatorSettings(int port, int pingFrequencySeconds, URI redirectUri) {
        this.port = port;
        this.pingFrequencySeconds = pingFrequencySeconds;
        this.redirectUri = redirectUri;
    }

    public int getPort() {
        return port;
    }

    public int getPingFrequencySeconds() {
        return pingFrequencySeconds;
    }

    public URI getRedirectUri() {
        return redirectUri;
    }

}
