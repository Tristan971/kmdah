package moe.tristan.kmdah.common.model.configuration;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kmdah.worker")
public class WorkerSettings {

    private int port;

    private URI operatorUri;

    private int bandwidthMbps;

    private boolean verifyReferrer;

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

    public boolean isVerifyReferrer() {
        return verifyReferrer;
    }

    public void setVerifyReferrer(boolean verifyReferrer) {
        this.verifyReferrer = verifyReferrer;
    }

}
