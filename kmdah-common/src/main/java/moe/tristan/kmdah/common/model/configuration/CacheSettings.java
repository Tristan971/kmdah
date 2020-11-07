package moe.tristan.kmdah.common.model.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kmdah.cache")
public class CacheSettings {

    private long maxSizeGibibytes;

    private String root;

    public long getMaxSizeGibibytes() {
        return maxSizeGibibytes;
    }

    /**
     * @param maxSizeGibibytes Maximal requested cache size
     *
     * @implNote The effective size will end up hovering *around* this value ; provision some extra storage (on the order of 15% or so, to be safe)
     */
    public void setMaxSizeGibibytes(long maxSizeGibibytes) {
        this.maxSizeGibibytes = maxSizeGibibytes;
    }

    public String getRoot() {
        return root;
    }

    /**
     * @param root root directory in which to put the cache ; trailing ({@code /} must not be present)
     */
    public void setRoot(String root) {
        this.root = root;
    }

}
