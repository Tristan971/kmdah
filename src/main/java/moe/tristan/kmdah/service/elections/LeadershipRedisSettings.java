package moe.tristan.kmdah.service.elections;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.leadership.redis")
public record LeadershipRedisSettings(

    String host,

    int port,

    String lockRegistryKey

) {}
