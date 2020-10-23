package moe.tristan.kmdah.common.mangadex.ping;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.treatwell.immutables.styles.ValueObjectStyle;

import moe.tristan.kmdah.common.mangadex.MangadexApi;

@Immutable
@ValueObjectStyle
abstract class AbstractPingRequest {

    /**
     * @return the client secret
     */
    public abstract String getSecret();

    /**
     * @return the client current port
     */
    public abstract int getPort();

    /**
     * @return the currently available diskspace (for client usage) in bytes
     */
    @JsonProperty("disk_space")
    public abstract int getDiskSpace();

    /**
     * @return the maximum speed to dedicate to the client, in kilobytes per second
     */
    @JsonProperty("network_speed")
    public abstract int getNetworkSpeed();

    /**
     * @return the TLS timestamp from the previous ping
     */
    @JsonProperty("tls_created_at")
    @JsonFormat(shape = STRING, pattern = MangadexApi.TIMESTAMP_FORMAT)
    public abstract Optional<ZonedDateTime> getTlsCreatedAt();

    /**
     * @return the client's build version
     */
    @Value.Derived
    @JsonProperty("build_version")
    public int getBuildVersion() {
        return MangadexApi.VERSION;
    }

}
