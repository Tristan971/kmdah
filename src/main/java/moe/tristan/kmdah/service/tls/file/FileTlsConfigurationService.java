package moe.tristan.kmdah.service.tls.file;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import moe.tristan.kmdah.mangadex.ping.TlsData;
import moe.tristan.kmdah.service.tls.TlsConfigurationService;
import moe.tristan.kmdah.service.tls.TlsDataReceivedEvent;

public class FileTlsConfigurationService implements TlsConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileTlsConfigurationService.class);

    private final FileTlsConfigurationSettings fileTlsConfigurationSettings;

    public FileTlsConfigurationService(FileTlsConfigurationSettings fileTlsConfigurationSettings) {
        this.fileTlsConfigurationSettings = fileTlsConfigurationSettings;
    }

    @Override
    public void applyTlsConfig(TlsDataReceivedEvent event) {
        TlsData tlsData = event.tlsData();
        Path certPath = fileTlsConfigurationSettings.certificateOutputFile();
        Path privKeyPath = fileTlsConfigurationSettings.privateKeyOutputFile();

        Map.of(
            certPath, tlsData.certificate(),
            privKeyPath, tlsData.privateKey()
        ).forEach((target, data) -> {
            try {
                Files.writeString(
                    target,
                    data,
                    CREATE, TRUNCATE_EXISTING
                );
                LOGGER.info("Wrote TLS data to {}", target.toAbsolutePath());
            } catch (IOException e) {
                throw new IllegalStateException("Cannot write to file " + target.toAbsolutePath(), e);
            }
        });

    }

}
