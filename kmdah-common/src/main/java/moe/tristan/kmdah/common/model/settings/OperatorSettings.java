package moe.tristan.kmdah.common.model.settings;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.operator")
public class OperatorSettings implements UserSettings {

    private final int port;
    private final int pingFrequencySeconds;
    private final URI workersUri;

    public OperatorSettings(int port, int pingFrequencySeconds, URI workersUri) {
        this.port = port;
        this.pingFrequencySeconds = pingFrequencySeconds;
        this.workersUri = workersUri;
    }

    public int getPort() {
        return port;
    }

    public int getPingFrequencySeconds() {
        return pingFrequencySeconds;
    }

    public URI getWorkersUri() {
        return workersUri;
    }

}
