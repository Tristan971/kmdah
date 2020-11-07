package moe.tristan.kmdah.common.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.cache")
public class CacheSettings {

    private final long maxSizeGibibytes;
    private final String root;

    public CacheSettings(long maxSizeGibibytes, String root) {
        this.maxSizeGibibytes = maxSizeGibibytes;
        this.root = root;
    }

    public long getMaxSizeGibibytes() {
        return maxSizeGibibytes;
    }

    public String getRoot() {
        return root;
    }

}
