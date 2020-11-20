package moe.tristan.kmdah.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.cache")
public class CacheSettings {

    private final int maxSizeGb;

    public CacheSettings(int maxSizeGb) {
        this.maxSizeGb = maxSizeGb;
    }

    public int getMaxSizeGb() {
        return maxSizeGb;
    }

}
