package moe.tristan.kmdah.mangadex;

import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Valid
@ConfigurationProperties("kmdah.mangadex")
public record MangaDexSettings(

    @NotBlank
    String clientSecret,

    @NotBlank
    @Pattern(regexp = "^([0-9]+\\.){3}[0-9]+$")
    String loadBalancerIp,

    @Positive
    long bandwidthMbps,

    boolean enforceTokens,

    Optional<@URL String> overrideApiUrl,

    Optional<@URL String> overrideUpstreamImageServer

) { }
