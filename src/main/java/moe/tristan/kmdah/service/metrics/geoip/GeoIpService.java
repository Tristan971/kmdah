package moe.tristan.kmdah.service.metrics.geoip;

import java.net.InetAddress;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.maxmind.geoip2.DatabaseReader;

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

    public Optional<String> resolveCountryCode(String address) {
        if (databaseReader == null) {
            return Optional.empty();
        } else {
            try {
                InetAddress inetAddress = InetAddress.getByName(address);
                return Optional.of(databaseReader.country(inetAddress).getCountry().getIsoCode());
            } catch (Throwable e) {
                LOGGER.warn("Couldn't resolve country for ip: {} - {}", address, e.getMessage());
                return Optional.empty();
            }
        }
    }

}
