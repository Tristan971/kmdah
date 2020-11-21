package moe.tristan.kmdah.mangadex.stop;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StopRequest(

    @JsonProperty("secret")
    String secret

) {}
