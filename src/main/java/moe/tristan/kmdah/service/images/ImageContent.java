package moe.tristan.kmdah.service.images;

import java.time.Instant;
import java.util.OptionalLong;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import moe.tristan.kmdah.service.images.cache.CacheMode;

public record ImageContent(

    Resource resource,

    MediaType contentType,

    OptionalLong contentLength,

    Instant lastModified,

    CacheMode cacheMode

) {}
