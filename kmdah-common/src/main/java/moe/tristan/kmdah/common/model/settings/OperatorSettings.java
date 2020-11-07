package moe.tristan.kmdah.common.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kmdah.operator")
public class OperatorSettings {

    private int port;

    private String secret;

    private int pingFrequencySeconds;

    private int gracefulShutdownSeconds;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getPingFrequencySeconds() {
        return pingFrequencySeconds;
    }

    public void setPingFrequencySeconds(int pingFrequencySeconds) {
        if (pingFrequencySeconds > 100) {
            throw new IllegalArgumentException(
                "The MDAH client spec requires a ping to consistently happen at least every 2 minutes, "
                + "but you asked for " + pingFrequencySeconds + " seconds."
            );
        }
        this.pingFrequencySeconds = pingFrequencySeconds;
    }

    public int getGracefulShutdownSeconds() {
        return gracefulShutdownSeconds;
    }

    public void setGracefulShutdownSeconds(int gracefulShutdownSeconds) {
        this.gracefulShutdownSeconds = gracefulShutdownSeconds;
    }

}
