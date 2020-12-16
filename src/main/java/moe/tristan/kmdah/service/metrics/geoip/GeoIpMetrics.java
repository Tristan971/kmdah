package moe.tristan.kmdah.service.metrics.geoip;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class GeoIpMetrics {

    private static final String GEOIP_METRIC = "kmdah_geoip_count";
    private static final String TAG_COUNTRY = "country";

    private final MeterRegistry meterRegistry;
    private final GeoIpSettings geoIpSettings;
    private final GeoIpService geoIpService;

    public GeoIpMetrics(MeterRegistry meterRegistry, GeoIpSettings geoIpSettings, GeoIpService geoIpService) {
        this.meterRegistry = meterRegistry;
        this.geoIpSettings = geoIpSettings;
        this.geoIpService = geoIpService;
    }

    public void recordCountrySource(String ipAddress) {
        if (geoIpSettings.enabled()) {
            geoIpService.resolveCountryCode(ipAddress).ifPresent(
                countryCode -> meterRegistry.counter(
                    GEOIP_METRIC,
                    TAG_COUNTRY, countryCode
                ).increment()
            );
        }
    }

}
