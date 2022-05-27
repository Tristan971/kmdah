package moe.tristan.kmdah.mangadex;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Valid
@ConfigurationProperties("kmdah.mangadex")
public record MangadexSettings(

    @NotBlank
    String clientSecret,

    @NotBlank
    @Pattern(regexp = "^(\\d+\\.){3}\\d+$")
    String loadBalancerIp,

    @Positive
    long bandwidthMbps,

    boolean enforceTokens

) { }
