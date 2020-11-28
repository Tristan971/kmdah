package moe.tristan.kmdah.service.sync;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("kmdah.id")
public class InstanceIdSettings {

    private final String id;

    @ConstructorBinding
    public InstanceIdSettings(IdGenerationMethod generationMethod) {
        this.id = resolveId(generationMethod);
    }

    public String getId() {
        return id;
    }

    public enum IdGenerationMethod {
        HOSTNAME,
        RANDOM_UUID
    }

    private static String resolveId(IdGenerationMethod method) {
        return switch (method) {
            case RANDOM_UUID -> UUID.randomUUID().toString();
            case HOSTNAME -> {
                try {
                    yield Inet4Address.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    throw new IllegalStateException("Couldn't resolve hostname!", e);
                }
            }
        };
    }

}
