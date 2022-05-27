package moe.tristan.kmdah.service.tls.k8s;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
import moe.tristan.kmdah.mangadex.ping.TlsData;
import moe.tristan.kmdah.service.tls.TlsConfigurationService;
import moe.tristan.kmdah.service.tls.TlsDataReceivedEvent;

public class K8sTlsConfigurationService implements TlsConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8sTlsConfigurationService.class);

    private final K8sTlsSecretSettings kubernetesTlsSecretSettings;
    private final CoreV1Api coreV1Api;

    private TlsData lastTlsData;

    public K8sTlsConfigurationService(K8sTlsSecretSettings secretSettings, CoreV1Api coreV1Api) {
        this.kubernetesTlsSecretSettings = secretSettings;
        this.coreV1Api = coreV1Api;
    }

    @EventListener(TlsDataReceivedEvent.class)
    @Override
    public void applyTlsConfig(TlsDataReceivedEvent event) {
        if (kubernetesTlsSecretSettings.autoUpdate()) {
            this.syncTlsData(event.tlsData());
        } else {
            LOGGER.info("Kubernetes secret auto-update disabled - ignoring TlsData");
        }
    }

    private void syncTlsData(TlsData tlsData) {
        if (tlsData.equals(lastTlsData)) {
            LOGGER.debug("Unchanged TlsData");
        }

        LOGGER.info("New TlsData - syncing tls cert secret as {}/{}", kubernetesTlsSecretSettings.namespace(), kubernetesTlsSecretSettings.name());

        V1Secret secret = buildSecretFromTlsData(tlsData);
        V1Secret result;
        try {
            result = coreV1Api.replaceNamespacedSecret(
                kubernetesTlsSecretSettings.name(),
                kubernetesTlsSecretSettings.namespace(),
                secret,
                "true",
                null,
                null,
                null
            );
        } catch (ApiException exception) {
            LOGGER.error("Failed secret synchronization!\n{}", exception.getResponseBody());
            throw new IllegalStateException("Unable to synchronize TLS secret", exception);
        }

        V1ObjectMeta resultMeta = requireNonNull(result.getMetadata());
        LOGGER.info(
            "Reconfigured secret {}/{} with latest TlsData",
            resultMeta.getNamespace(),
            resultMeta.getName()
        );
        lastTlsData = tlsData;
    }

    private V1Secret buildSecretFromTlsData(TlsData tlsData) {
        V1Secret secret = new V1Secret();
        secret.setType("kubernetes.io/tls");

        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setNamespace(requireNonNull(kubernetesTlsSecretSettings.namespace(), "Tls secret namespace is null."));
        metadata.setName(requireNonNull(kubernetesTlsSecretSettings.name(), "Tls secret name is null."));
        secret.setMetadata(metadata);

        Map<String, byte[]> secretData = new HashMap<>(3);
        secretData.put("tls.crt", tlsData.certificate().getBytes(StandardCharsets.UTF_8));
        secretData.put("tls.key", tlsData.privateKey().getBytes(StandardCharsets.UTF_8));
        secret.setData(secretData);

        return secret;
    }

}
