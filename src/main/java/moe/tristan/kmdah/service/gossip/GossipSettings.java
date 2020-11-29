package moe.tristan.kmdah.service.gossip;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import moe.tristan.kmdah.service.gossip.InstanceId.IdGenerationStrategy;

@ConstructorBinding
@ConfigurationProperties("kmdah.gossip")
public record GossipSettings(
    IdGenerationStrategy idGenerationStrategy
) {}
