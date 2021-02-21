package moe.tristan.kmdah.mangadex.image;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static moe.tristan.kmdah.mangadex.MangadexApi.TIMESTAMP_FORMAT;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ImageToken(

    @JsonProperty("expires")
    @JsonFormat(shape = STRING, pattern = TIMESTAMP_FORMAT)
    ZonedDateTime expires,

    @JsonProperty("hash")
    String hash

) {}
