package moe.tristan.kmdah.service.metrics.geoip;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("kmdah.geoip")
@ConstructorBinding
public record GeoIpSettings(

    boolean enabled,

    String ipHeader,

    String licenseKey

) {}
