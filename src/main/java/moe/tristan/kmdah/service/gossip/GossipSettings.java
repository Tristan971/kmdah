package moe.tristan.kmdah.service.gossip;

import org.springframework.boot.context.properties.ConfigurationProperties;

import moe.tristan.kmdah.service.gossip.InstanceId.IdGenerationStrategy;

@ConfigurationProperties("kmdah.gossip")
public record GossipSettings(
    IdGenerationStrategy idGenerationStrategy
) {}
