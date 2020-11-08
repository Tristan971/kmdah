package moe.tristan.kmdah.common.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.cache")
public class CacheSettings {

    private final String root;
    private final long sizeGib;

    public CacheSettings(String root, long sizeGib) {
        this.root = root;
        this.sizeGib = sizeGib;
    }

    public String getRoot() {
        return root;
    }

    public long getSizeGib() {
        return sizeGib;
    }

}
