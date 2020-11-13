package moe.tristan.kmdah.operator.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.operator.cache")
public class CacheSettings {

    private final int maxSizeGb;

    public CacheSettings(int maxSizeGb) {
        this.maxSizeGb = maxSizeGb;
    }

    public int getMaxSizeGb() {
        return maxSizeGb;
    }

}
