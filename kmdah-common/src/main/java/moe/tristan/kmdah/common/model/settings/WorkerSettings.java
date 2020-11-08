package moe.tristan.kmdah.common.model.settings;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.worker")
public class WorkerSettings {

    private final int port;
    private final URI operatorUri;
    private final int bandwidthMbps;
    private final boolean verifyReferrer;

    public WorkerSettings(int port, URI operatorUri, int bandwidthMbps, boolean verifyReferrer) {
        this.port = port;
        this.operatorUri = operatorUri;
        this.bandwidthMbps = bandwidthMbps;
        this.verifyReferrer = verifyReferrer;
    }

    public int getPort() {
        return port;
    }

    public URI getOperatorUri() {
        return operatorUri;
    }

    public int getBandwidthMbps() {
        return bandwidthMbps;
    }

    public boolean isVerifyReferrer() {
        return verifyReferrer;
    }

}
