package moe.tristan.kmdah.operator.service.kubernetes;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.common.model.mangadex.ping.TlsData;
import moe.tristan.kmdah.common.model.settings.TlsSecretSettings;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;

@Service
public class KubernetesIngressTlsSecretService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesIngressTlsSecretService.class);

    private final TlsSecretSettings tlsSecretSettings;
    private final CoreV1Api coreV1Api;

    private TlsData lastTlsData;

    public KubernetesIngressTlsSecretService(TlsSecretSettings tlsSecretSettings, CoreV1Api kubernetesCoreV1Api) {
        this.tlsSecretSettings = tlsSecretSettings;
        this.coreV1Api = kubernetesCoreV1Api;
    }

    public void syncTlsData(TlsData tlsData) {
        if (tlsData.equals(lastTlsData)) {
            LOGGER.debug("Unchanged TlsData");
        }

        LOGGER.info("New TlsData is different from the previous, so updating tls cert secret");

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
        metadata.setNamespace(requireNonNull(tlsSecretSettings.getNamespace(), "Tls secret namespace is null."));
        metadata.setName(requireNonNull(tlsSecretSettings.getName(), "Tls secret name is null."));
        secret.setMetadata(metadata);

        Map<String, byte[]> secretData = new HashMap<>(3);
        secretData.put("tls.crt", tlsData.getCertificate().getBytes(StandardCharsets.UTF_8));
        secretData.put("tls.key", tlsData.getPrivateKey().getBytes(StandardCharsets.UTF_8));
        secret.setData(secretData);

        return secret;
    }

}
