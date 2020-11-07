package moe.tristan.kmdah.common.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.metadata")
public class MetadataSettings {

    private final String serverHeader;
    private final int clientSpec;

    public MetadataSettings(String serverHeader, int clientSpec) {
        this.serverHeader = serverHeader;
        this.clientSpec = clientSpec;
    }

    public String getServerHeader() {
        return serverHeader;
    }

    public int getClientSpec() {
        return clientSpec;
    }

}
