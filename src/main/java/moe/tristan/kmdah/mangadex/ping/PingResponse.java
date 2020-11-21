package moe.tristan.kmdah.mangadex.ping;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PingResponse(

    @JsonProperty("image_server")
    String imageServer,

    @JsonProperty("latest_build")
    String latestBuild,

    @JsonProperty("url")
    String url,

    @JsonProperty("token_key")
    String tokenKey,

    @JsonProperty("compromised")
    boolean compromised,

    @JsonProperty("paused")
    boolean paused,

    @JsonProperty("tls")
    @JsonInclude(NON_EMPTY)
    Optional<TlsData> tls

) {}
