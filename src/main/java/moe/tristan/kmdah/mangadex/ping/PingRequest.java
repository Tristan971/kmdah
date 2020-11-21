package moe.tristan.kmdah.mangadex.ping;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import moe.tristan.kmdah.mangadex.MangadexApi;

public record PingRequest(

    @JsonProperty("secret")
    String secret,

    @JsonProperty("port")
    int port,

    @JsonProperty("disk_space")
    long diskSpace,

    @JsonProperty("network_speed")
    long networkSpeed,

    @JsonProperty("tls_created_at")
    @JsonInclude(Include.NON_EMPTY)
    @JsonFormat(shape = STRING, pattern = MangadexApi.TIMESTAMP_FORMAT)
    Optional<ZonedDateTime> tlsCreatedAt,

    @JsonProperty("build_version")
    int specVersion

) {}
