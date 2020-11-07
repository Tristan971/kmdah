package moe.tristan.kmdah.common.model.mangadex.ping;

import java.net.URL;
import java.util.Optional;

import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Redacted;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractPingResponse {

    /**
     * @return the image server to grab images from
     */
    @JsonProperty("image_server")
    public abstract String getImageServer();

    /**
     * @return the latest build version of the official client
     */
    @JsonProperty("latest_build")
    public abstract String getLatestBuild();

    /**
     * @return the URL assigned to this client
     */
    public abstract URL getUrl();

    /**
     * @return a base64 encoded key that is the shared key for token decoding
     */
    @Redacted
    @JsonProperty("token_key")
    public abstract String getTokenKey();

    /**
     * @return whether the node is marked as compromised due to a lot of IP address changes
     */
    public abstract boolean isCompromised();

    /**
     * @return whether the node has been manually paused
     */
    public abstract boolean isPaused();

    /**
     * @return the TLS signing information
     */
    @Auxiliary
    public abstract Optional<TlsData> getTls();

}
