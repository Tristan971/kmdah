package moe.tristan.kmdah.service.metrics.geoip;

import java.net.InetAddress;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maxmind.geoip2.DatabaseReader;

public class GeoIpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoIpService.class);

    private final DatabaseReader databaseReader;

    public GeoIpService(DatabaseReader databaseReader) {
        this.databaseReader = databaseReader;
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
