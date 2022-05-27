package moe.tristan.kmdah.service.images.cache;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Valid
@ConfigurationProperties("kmdah.cache")
public record CacheSettings(

    @NotNull
    CacheBackend backend,

    @Positive
    int maxSizeGb,

    @PositiveOrZero
    long abortLookupThresholdMillis

) { }
