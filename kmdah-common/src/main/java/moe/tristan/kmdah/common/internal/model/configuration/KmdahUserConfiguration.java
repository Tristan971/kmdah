package moe.tristan.kmdah.common.internal.model.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kmdah")
public class KmdahUserConfiguration {

    private CacheSettings cacheSettings;

    public CacheSettings getCacheSettings() {
        return cacheSettings;
    }

    public void setCacheSettings(CacheSettings cacheSettings) {
        this.cacheSettings = cacheSettings;
    }

}
