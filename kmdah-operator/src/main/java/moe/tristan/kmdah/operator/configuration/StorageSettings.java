package moe.tristan.kmdah.operator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kmdah.storage")
public class StorageSettings {

    private long cacheSizeMebibytes;

    public long getCacheSizeMebibytes() {
        return cacheSizeMebibytes;
    }

    public void setCacheSizeMebibytes(long cacheSizeMebibytes) {
        this.cacheSizeMebibytes = cacheSizeMebibytes;
    }

}
