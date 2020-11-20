package moe.tristan.kmdah.model;

import java.util.Optional;
import java.util.OptionalLong;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import moe.tristan.kmdah.cache.CacheMode;

public record ImageContent(

    Flux<DataBuffer> bytes,
    Optional<MediaType> contentType,
    OptionalLong contentLength,
    CacheMode getCacheMode

) {}
