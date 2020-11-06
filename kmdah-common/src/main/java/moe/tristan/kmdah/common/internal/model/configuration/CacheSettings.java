package moe.tristan.kmdah.common.internal.model.configuration;

public class CacheSettings {

    private int maxSizeMegabytes;

    private String root;

    public int getMaxSizeMegabytes() {
        return maxSizeMegabytes;
    }

    public void setMaxSizeMegabytes(int maxSizeMegabytes) {
        this.maxSizeMegabytes = maxSizeMegabytes;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

}
