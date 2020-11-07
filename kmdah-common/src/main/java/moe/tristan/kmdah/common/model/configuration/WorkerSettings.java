package moe.tristan.kmdah.common.model.configuration;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kmdah.worker")
public class WorkerSettings {

    private int port;

    public URI operatorUri;

    public int bandwidthMbps;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public URI getOperatorUri() {
        return operatorUri;
    }

    public void setOperatorUri(URI operatorUri) {
        this.operatorUri = operatorUri;
    }

    public int getBandwidthMbps() {
        return bandwidthMbps;
    }

    public void setBandwidthMbps(int bandwidthMbps) {
        this.bandwidthMbps = bandwidthMbps;
    }

}
