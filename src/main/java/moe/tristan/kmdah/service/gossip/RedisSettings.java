package moe.tristan.kmdah.service.gossip;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kmdah.gossip.redis")
public record RedisSettings(

    String host,

    int port,

    String lockRegistryKey,

    String gossipTopic

) {}
