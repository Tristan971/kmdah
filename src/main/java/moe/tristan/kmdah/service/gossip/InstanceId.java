package moe.tristan.kmdah.service.gossip;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

public record InstanceId(

    String id

) {

    public static String generateId(IdGenerationStrategy idGenerationStrategy) {
        return switch (idGenerationStrategy) {
            case RANDOM_UUID -> uuid();
            case HOSTNAME -> hostname();
        };
    }

    private static String uuid() {
        return UUID.randomUUID().toString();
    }

    private static String hostname() {
        try {
            return Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Couldn't resolve hostname!", e);
        }
    }

    public enum IdGenerationStrategy {
        HOSTNAME,
        RANDOM_UUID
    }

}
