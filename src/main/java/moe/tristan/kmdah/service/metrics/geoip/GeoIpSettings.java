package moe.tristan.kmdah.service.metrics.geoip;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kmdah.geoip")
public record GeoIpSettings(

    boolean enabled,

    String licenseKey

) {}
