package moe.tristan.kmdah.service.gossip;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("kmdah.gossip.redis")
public record RedisSettings(

    String host,

    int port,

    String lockRegistryKey,

    String gossipTopic

) {}
