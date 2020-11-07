package moe.tristan.kmdah.common.model.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kmdah.cache")
public class CacheSettings {

    private long maxSizeGigabytes;

    private String root;

    public long getMaxSizeGigabytes() {
        return maxSizeGigabytes;
    }

    public void setMaxSizeGigabytes(long maxSizeGigabytes) {
        this.maxSizeGigabytes = maxSizeGigabytes;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

}
