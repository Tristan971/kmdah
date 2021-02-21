package moe.tristan.kmdah.mangadex.image;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageToken(

    @JsonProperty("expires")
    ZonedDateTime expires,

    @JsonProperty("hash")
    String hash

) {}
