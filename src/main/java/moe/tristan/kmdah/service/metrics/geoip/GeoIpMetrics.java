package moe.tristan.kmdah.service.metrics.geoip;

import io.micrometer.core.instrument.MeterRegistry;

public class GeoIpMetrics {

    private static final String GEOIP_METRIC = "kmdah_geoip_count";
    private static final String TAG_COUNTRY = "country";

    private final MeterRegistry meterRegistry;
    private final GeoIpService geoIpService;

    public GeoIpMetrics(MeterRegistry meterRegistry, GeoIpService geoIpService) {
        this.meterRegistry = meterRegistry;
        this.geoIpService = geoIpService;
    }

    public void recordCountrySource(String ipAddress) {
        geoIpService.resolveCountryCode(ipAddress).ifPresent(
            countryCode -> meterRegistry.counter(
                GEOIP_METRIC,
                TAG_COUNTRY, countryCode
            ).increment()
        );
    }

}
