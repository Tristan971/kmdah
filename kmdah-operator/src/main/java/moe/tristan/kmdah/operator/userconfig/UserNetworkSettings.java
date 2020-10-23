package moe.tristan.kmdah.operator.userconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("kmdah.network")
public class UserNetworkSettings {

    private int port;
    private int bandwidthMegabytesPerSecond;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBandwidthMegabytesPerSecond() {
        return bandwidthMegabytesPerSecond;
    }

    public void setBandwidthMegabytesPerSecond(int bandwidthMegabytesPerSecond) {
        this.bandwidthMegabytesPerSecond = bandwidthMegabytesPerSecond;
    }

}
