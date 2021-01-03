package moe.tristan.kmdah.service.images;

import java.time.Instant;
import java.util.OptionalLong;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import moe.tristan.kmdah.service.images.cache.CacheMode;

public record ImageContent(

    Flux<DataBuffer> bytes,

    MediaType contentType,

    OptionalLong contentLength,

    Instant lastModified,

    CacheMode cacheMode

) {}
