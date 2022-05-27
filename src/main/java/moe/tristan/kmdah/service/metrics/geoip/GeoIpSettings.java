package moe.tristan.kmdah.service.metrics.geoip;

import jakarta.validation.Valid;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Valid
@ConfigurationProperties("kmdah.geoip")
public record GeoIpSettings(

    boolean enabled,

    String licenseKey

) { }
