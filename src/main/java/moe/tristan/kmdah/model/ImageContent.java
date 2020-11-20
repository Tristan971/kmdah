package moe.tristan.kmdah.model;

import java.awt.image.DataBuffer;
import java.util.Optional;
import java.util.OptionalLong;

import org.springframework.http.MediaType;

import moe.tristan.kmdah.cache.CacheMode;
import reactor.core.publisher.Flux;

public record ImageContent(

    Flux<DataBuffer> bytes,
    Optional<MediaType> contentType,
    OptionalLong contentLength,
    CacheMode getCacheMode

) {}
