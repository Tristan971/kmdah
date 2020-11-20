package moe.tristan.kmdah.model;

import java.io.InputStream;

import org.springframework.http.MediaType;

import moe.tristan.kmdah.cache.CacheMode;

public record ImageContent(

    InputStream inputStream,
    MediaType contentType,
    long contentLength,
    CacheMode getCacheMode

) {}
