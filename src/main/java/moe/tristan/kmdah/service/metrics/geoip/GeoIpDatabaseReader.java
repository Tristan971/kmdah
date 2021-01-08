package moe.tristan.kmdah.service.metrics.geoip;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;

@Component
public class GeoIpDatabaseReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoIpDatabaseReader.class);

    private static final String GEOIP_COUNTRY_URI_FORMAT = "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country&license_key={license}&suffix=tar.gz";

    private final RestTemplate restTemplate;

    public GeoIpDatabaseReader(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DatabaseReader newDatabaseReader(String geoIpLicenseKey) {
        try {
            Path databaseFileDir = Files.createTempDirectory("kmdah-geoip");
            Path downloadLocation = Files.createTempFile(databaseFileDir, "download", ".tar.gz");

            byte[] geoIpDatabase = restTemplate.getForObject(
                UriComponentsBuilder.fromHttpUrl(GEOIP_COUNTRY_URI_FORMAT).buildAndExpand(geoIpLicenseKey).toUriString(),
                byte[].class
            );

            Files.write(downloadLocation, requireNonNull(geoIpDatabase));

            Path databaseFile = untarDatabase(databaseFileDir, downloadLocation);

            try {
                LOGGER.info("Opening GeoIp database file at {}", databaseFile);
                return new DatabaseReader.Builder(requireNonNull(databaseFile).toFile()).withCache(new CHMCache()).build();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path untarDatabase(Path cwd, Path tarFile) {
        try {
            Process untarProcess = new ProcessBuilder()
                .directory(cwd.toFile())
                .command("/usr/bin/env", "tar", "xfv", tarFile.toAbsolutePath().toString())
                .inheritIO()
                .start();

            int retCode = untarProcess.waitFor();
            LOGGER.info("Database decompression result: {}", retCode);

            return Files
                .walk(cwd)
                .filter(path -> path.getFileName().toString().endsWith(".mmdb"))
                .findAny()
                .orElseThrow()
                .toAbsolutePath();
        } catch (InterruptedException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
