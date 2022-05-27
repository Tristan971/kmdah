package moe.tristan.kmdah.service.gossip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

import moe.tristan.kmdah.service.gossip.InstanceId.IdGenerationStrategy;

@Valid
@ConfigurationProperties("kmdah.gossip")
public record GossipSettings(

    @NotNull
    IdGenerationStrategy idGenerationStrategy

) { }
