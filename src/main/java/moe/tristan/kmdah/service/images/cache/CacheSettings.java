package moe.tristan.kmdah.service.images.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.cache")
public record CacheSettings(

    CacheBackend backend,

    int maxSizeGb

) {}
