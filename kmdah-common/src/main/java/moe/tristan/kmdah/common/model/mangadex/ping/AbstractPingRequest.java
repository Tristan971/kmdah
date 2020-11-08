package moe.tristan.kmdah.common.model.mangadex.ping;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Redacted;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.treatwell.immutables.styles.ValueObjectStyle;

import moe.tristan.kmdah.common.model.mangadex.MangadexApi;

@Immutable
@ValueObjectStyle
abstract class AbstractPingRequest {

    /**
     * @return the client secret
     */
    @Redacted
    public abstract String getSecret();

    /**
     * @return the client current port
     */
    public abstract int getPort();

    /**
     * @return the currently available diskspace (for client usage) in bytes
     */
    @JsonProperty("disk_space")
    public abstract long getDiskSpace();

    /**
     * @return the maximum speed to dedicate to the client, in kilobytes per second
     */
    @JsonProperty("network_speed")
    public abstract long getNetworkSpeed();

    /**
     * @return the TLS timestamp from the previous ping
     */
    @JsonProperty("tls_created_at")
    @JsonFormat(shape = STRING, pattern = MangadexApi.TIMESTAMP_FORMAT)
    public abstract Optional<ZonedDateTime> getTlsCreatedAt();

    /**
     * @return the latest spec version that this client is compatible with
     */
    @JsonProperty("build_version")
    public abstract int getSpecVersion();

}
