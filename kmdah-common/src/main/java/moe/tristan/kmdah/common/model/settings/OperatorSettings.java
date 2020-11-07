package moe.tristan.kmdah.common.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.operator")
public class OperatorSettings {

    private final String secret;
    private final int port;
    private final int pingFrequencySeconds;
    private final int gracefulShutdownSeconds;

    public OperatorSettings(String secret, int port, int pingFrequencySeconds, int gracefulShutdownSeconds) {
        this.secret = secret;
        this.port = port;
        this.pingFrequencySeconds = pingFrequencySeconds;
        this.gracefulShutdownSeconds = gracefulShutdownSeconds;

        if (pingFrequencySeconds > 100) {
            throw new IllegalArgumentException(
                "The MDAH client spec requires a ping to consistently happen at least every 2 minutes, "
                + "but you asked for " + pingFrequencySeconds + " seconds."
            );
        }
    }

    public String getSecret() {
        return secret;
    }

    public int getPort() {
        return port;
    }

    public int getPingFrequencySeconds() {
        return pingFrequencySeconds;
    }

    public int getGracefulShutdownSeconds() {
        return gracefulShutdownSeconds;
    }

}
