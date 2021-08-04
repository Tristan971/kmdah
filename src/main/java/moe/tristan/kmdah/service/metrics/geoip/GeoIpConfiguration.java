package moe.tristan.kmdah.service.metrics.geoip;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.maxmind.geoip2.DatabaseReader;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
@ConditionalOnProperty(name = "kmdah.geoip.enabled", havingValue = "true")
public class GeoIpConfiguration {

    @Bean
    public DatabaseReader geoIpDatabaseReader(GeoIpSettings geoIpSettings, RestTemplate restTemplate) {
        String geoIpLicenseKey = geoIpSettings.licenseKey();
        if (geoIpLicenseKey == null || geoIpLicenseKey.isBlank()) {
            throw new IllegalStateException("Cannot enable GeoIP, no license key provided!");
        }

        return new GeoIpDatabaseReader(restTemplate).newDatabaseReader(geoIpLicenseKey);
    }

    @Bean
    public GeoIpService geoIpService(DatabaseReader databaseReader) {
        return new GeoIpService(databaseReader);
    }

    @Bean
    public GeoIpMetrics geoIpMetrics(MeterRegistry meterRegistry, GeoIpService geoIpService) {
        return new GeoIpMetrics(meterRegistry, geoIpService);
    }

    @Bean
    public GeoIpMetricsFilter geoIpMetricsFilter(GeoIpMetrics geoIpMetrics) {
        return new GeoIpMetricsFilter(geoIpMetrics);
    }

}
