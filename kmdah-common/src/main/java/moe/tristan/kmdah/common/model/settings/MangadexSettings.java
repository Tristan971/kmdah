package moe.tristan.kmdah.common.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.mangadex")
public class MangadexSettings implements UserSettings {

    private final String clientSecret;
    private final int gracefulShutdownSeconds;

    public MangadexSettings(String clientSecret, int gracefulShutdownSeconds) {
        this.clientSecret = clientSecret;
        this.gracefulShutdownSeconds = gracefulShutdownSeconds;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public int getGracefulShutdownSeconds() {
        return gracefulShutdownSeconds;
    }

}
