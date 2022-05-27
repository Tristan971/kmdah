package moe.tristan.kmdah.service.gossip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Range;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Valid
@ConfigurationProperties("kmdah.gossip.redis")
public record RedisSettings(

    @NotBlank
    String host,

    @Range(min = 0, max = 65535)
    int port,

    @NotBlank
    String lockRegistryKey,

    @NotBlank
    String gossipTopic

) { }
