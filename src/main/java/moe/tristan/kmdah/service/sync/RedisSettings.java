package moe.tristan.kmdah.service.sync;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.redis")
public record RedisSettings(

    String host,

    int port,

    String lockRegistryKey,

    String topic

) {}
