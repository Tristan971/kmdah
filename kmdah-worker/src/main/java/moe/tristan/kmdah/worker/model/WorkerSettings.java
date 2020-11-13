package moe.tristan.kmdah.worker.model;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.worker")
public class WorkerSettings {

    private final int port;
    private final URI operatorUri;
    private final int bandwidthMbps;

    public WorkerSettings(int port, URI operatorUri, int bandwidthMbps) {
        this.port = port;
        this.operatorUri = operatorUri;
        this.bandwidthMbps = bandwidthMbps;
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

}
