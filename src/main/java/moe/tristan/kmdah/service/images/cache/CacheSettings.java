package moe.tristan.kmdah.service.images.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kmdah.cache")
public record CacheSettings(

    CacheBackend backend,

    int maxSizeGb,

    long abortLookupThresholdMillis

) {}
