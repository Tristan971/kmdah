package moe.tristan.kmdah.mangadex.image;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import moe.tristan.kmdah.mangadex.MangadexApi;

public record ImageToken(
    @JsonFormat(shape = STRING, pattern = MangadexApi.TIMESTAMP_FORMAT)
    ZonedDateTime expires,
    String hash
) {}
