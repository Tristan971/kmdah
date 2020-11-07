package moe.tristan.kmdah.common.model.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kmdah.metadata")
public class MetadataSettings {

    private String serverHeader;

    private int clientSpec;

    public String getServerHeader() {
        return serverHeader;
    }

    public void setServerHeader(String serverHeader) {
        this.serverHeader = serverHeader;
    }

    public int getClientSpec() {
        return clientSpec;
    }

    public void setClientSpec(int clientSpec) {
        this.clientSpec = clientSpec;
    }

}
