package moe.tristan.kmdah.mangadex.ping;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import moe.tristan.kmdah.mangadex.MangadexApi;

public record TlsData(

    @JsonProperty("created_at")
    @JsonFormat(shape = STRING, pattern = MangadexApi.TIMESTAMP_FORMAT)
    LocalDateTime createdAt,

    @JsonProperty("private_key")
    String privateKey,

    @JsonProperty("certificate")
    String certificate

) {}
