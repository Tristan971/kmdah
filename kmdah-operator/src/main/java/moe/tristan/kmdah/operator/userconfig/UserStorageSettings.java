package moe.tristan.kmdah.operator.userconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kmdah.storage")
public class UserStorageSettings {

    private long cacheSizeMebibytes;

    public long getCacheSizeMebibytes() {
        return cacheSizeMebibytes;
    }

    public void setCacheSizeMebibytes(long cacheSizeMebibytes) {
        this.cacheSizeMebibytes = cacheSizeMebibytes;
    }

}
