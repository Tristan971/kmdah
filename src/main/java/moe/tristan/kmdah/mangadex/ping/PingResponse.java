package moe.tristan.kmdah.mangadex.ping;

import java.net.URL;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PingResponse(

    @JsonProperty("image_server")
    String imageServer,

    @JsonProperty("latest_build")
    String latestBuild,

    URL url,

    @JsonProperty("token_key")
    String tokenKey,

    boolean compromised,

    boolean paused,

    Optional<TlsData> tls

) {}
