package moe.tristan.kmdah.service.kubernetes;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
import moe.tristan.kmdah.mangadex.ping.TlsData;
import moe.tristan.kmdah.model.event.PingResponseReceivedEvent;
import moe.tristan.kmdah.model.settings.KubernetesTlsSecretSettings;

@Service
public class IngressTlsSecretService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngressTlsSecretService.class);

    private final KubernetesTlsSecretSettings kubernetesTlsSecretSettings;
    private final CoreV1Api coreV1Api;

    private TlsData lastTlsData;

    public IngressTlsSecretService(KubernetesTlsSecretSettings kubernetesTlsSecretSettings, CoreV1Api kubernetesCoreV1Api) {
        this.kubernetesTlsSecretSettings = kubernetesTlsSecretSettings;
        this.coreV1Api = kubernetesCoreV1Api;
    }

    @EventListener(PingResponseReceivedEvent.class)
    public void pingReceived(PingResponseReceivedEvent event) {
        event.pingResponse().tls().ifPresent(this::syncTlsData);
    }

    private void syncTlsData(TlsData tlsData) {
        if (tlsData.equals(lastTlsData)) {
            LOGGER.debug("Unchanged TlsData");
        }

        LOGGER.info("New TlsData - syncing tls cert secret");

        V1Secret secret = buildSecretFromTlsData(tlsData);

        V1Secret result;
        try {
            result = createSecret(secret);
        } catch (ApiException createException) {
            try {
                result = updateSecret(secret);
            } catch (ApiException updateException) {
                updateException.printStackTrace();
                throw new IllegalStateException(
                    "Could not "
                        + "create (" + createException.getMessage() + ") "
                        + "nor update (" + updateException.getMessage() + ") "
                        + "secret!"
                );
            }
        }

        V1ObjectMeta resultMeta = requireNonNull(result.getMetadata());
        LOGGER.info(
            "Configured secret {}/{} with latest TlsData",
            resultMeta.getNamespace(),
            resultMeta.getName()
        );
        lastTlsData = tlsData;
    }

    private V1Secret createSecret(V1Secret secret) throws ApiException {
        V1ObjectMeta metadata = requireNonNull(secret.getMetadata());
        return coreV1Api.createNamespacedSecret(
            metadata.getNamespace(),
            secret,
            "true",
            null,
            null
        );
    }

    private V1Secret updateSecret(V1Secret secret) throws ApiException {
        V1ObjectMeta metadata = requireNonNull(secret.getMetadata());
        return coreV1Api.replaceNamespacedSecret(
            metadata.getName(),
            metadata.getNamespace(),
            secret,
            "true",
            null,
            null
        );
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
