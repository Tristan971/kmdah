package moe.tristan.kmdah.common.model.mangadex.ping;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import java.time.ZonedDateTime;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.treatwell.immutables.styles.ValueObjectStyle;

import moe.tristan.kmdah.common.model.mangadex.MangadexApi;

@Immutable
@ValueObjectStyle
abstract class AbstractTlsData {

    /**
     * @return a timestamp in RFC-3339 used in following {@link PingRequest}s.
     */
    @JsonProperty("created_at")
    @JsonFormat(shape = STRING, pattern = MangadexApi.TIMESTAMP_FORMAT)
    public abstract ZonedDateTime getCreatedAt();

    /**
     * @return a PKCS_1 private key
     */
    @JsonProperty("private_key")
    public abstract String getPrivateKey();

    /**
     * @return an x509 certificate usable to do TLS termination on the latest assigned domain name
     */
    public abstract String getCertificate();

}
