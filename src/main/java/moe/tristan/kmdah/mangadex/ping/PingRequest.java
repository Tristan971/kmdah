package moe.tristan.kmdah.mangadex.ping;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import moe.tristan.kmdah.mangadex.MangadexApi;

public record PingRequest(
    String secret,

    int port,

    @JsonProperty("disk_space")
    long diskSpace,

    @JsonProperty("network_speed")
    long networkSpeed,

    @JsonProperty("tls_created_at")
    @JsonFormat(shape = STRING, pattern = MangadexApi.TIMESTAMP_FORMAT)
    Optional<ZonedDateTime> tlsCreatedAt,

    @JsonProperty("build_version")
    int specVersion
) {}
