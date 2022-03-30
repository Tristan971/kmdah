package moe.tristan.kmdah.mangadex;

import java.net.Inet4Address;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kmdah.mangadex")
public record MangadexSettings(

    String clientSecret,

    Inet4Address loadBalancerIp,

    long bandwidthMbps,

    boolean enforceTokens

) {}
