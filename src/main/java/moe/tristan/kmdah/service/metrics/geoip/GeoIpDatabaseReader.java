package moe.tristan.kmdah.service.metrics.geoip;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;

@Component
public class GeoIpDatabaseReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoIpDatabaseReader.class);

    private static final String GEOIP_COUNTRY_URI_FORMAT = "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-Country&license_key={license}&suffix=tar.gz";

    private final WebClient webClient;

    public GeoIpDatabaseReader(WebClient.Builder webClient) {
        this.webClient = webClient.build();
    }

    public DatabaseReader newDatabaseReader(String geoIpLicenseKey) {
        try {
            Path databaseFileDir = Files.createTempDirectory("kmdah-geoip");
            Path downloadLocation = Files.createTempFile(databaseFileDir, "download", ".tar.gz");

            Flux<DataBuffer> geoIpDownload = webClient
                .get()
                .uri(GEOIP_COUNTRY_URI_FORMAT, geoIpLicenseKey)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .doOnSubscribe(__ -> LOGGER.info("Downloading GeoIp database to {}", downloadLocation.toAbsolutePath().toString()));

            DataBufferUtils
                .write(geoIpDownload, downloadLocation)
                .block();

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
