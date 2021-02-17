package moe.tristan.kmdah.service.tls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class TlsDataEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TlsDataEventListener.class);

    private final TlsConfigurationService tlsConfigurationService;

    public TlsDataEventListener(TlsConfigurationService tlsConfigurationService) {
        this.tlsConfigurationService = tlsConfigurationService;
    }

    @EventListener(TlsDataReceivedEvent.class)
    public void tlsDataReceivedEvent(TlsDataReceivedEvent event) {
        LOGGER.info("Received TLS data event: {}", event);
        tlsConfigurationService.applyTlsConfig(event);
        LOGGER.info("TLS data event processed successfully.");
    }

}
