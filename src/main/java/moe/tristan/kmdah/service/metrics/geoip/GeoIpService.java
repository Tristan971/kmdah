package moe.tristan.kmdah.service.metrics.geoip;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.AbstractCountryResponse;
import com.maxmind.geoip2.record.Country;

@Component
public class GeoIpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoIpService.class);

    private DatabaseReader databaseReader;

    public GeoIpService(GeoIpSettings geoIpSettings, GeoIpDatabaseReader geoIpDatabaseReader) {
        if (geoIpSettings.enabled()) {
            LOGGER.info("Initializing the GeoIp support...");
            databaseReader = geoIpDatabaseReader.newDatabaseReader(geoIpSettings.licenseKey());
            LOGGER.info("GeoIp support initialized!");
        } else {
            LOGGER.info("GeoIp support is not enabled.");
        }
    }

    public Optional<String> resolveCountryCode(InetAddress inetAddress) {
        if (databaseReader == null) {
            throw new IllegalStateException("Cannot resolve IP address' country when the GeoIp support is not enabled!");
        } else {
            try {
                return databaseReader.tryCountry(inetAddress).map(AbstractCountryResponse::getCountry).map(Country::getIsoCode);
            } catch (IOException | GeoIp2Exception e) {
                throw new IllegalStateException("Couldn't resolve country for " + inetAddress);
            }
        }
    }

}
