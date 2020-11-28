package moe.tristan.kmdah.service.elections;

import static java.util.Objects.requireNonNull;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.leader.AbstractCandidate;
import org.springframework.integration.leader.Context;
import org.springframework.stereotype.Component;

@Component
public class LeadershipCandidate extends AbstractCandidate {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeadershipCandidate.class);

    public LeadershipCandidate(CandidacySettings candidacySettings) {
        super(resolveId(candidacySettings), null);
        LOGGER.info("Will participate in leadership elections for role [{}] as [{}]", getRole(), getId());
    }

    @Override
    public void onGranted(Context ctx) throws InterruptedException {

    }

    @Override
    public void onRevoked(Context ctx) {

    }

    private static String resolveId(CandidacySettings candidacySettings) {
        return switch (candidacySettings.idGenerationMethod()) {
            case RANDOM_UUID -> {
                LOGGER.info("Generating candidacy id from a random UUID");
                yield UUID.randomUUID().toString();
            }
            case HOSTNAME -> {
                LOGGER.info("Generating candidacy id from hostname");
                yield hostname();
            }
            case STATIC -> {
                LOGGER.info("Using static candidacy id");
                String staticId = requireNonNull(
                    candidacySettings.staticId(),
                    "When using candidacy id generation method STATIC, you must set an explicit id value!"
                );
                if (staticId.isBlank()) {
                    throw new IllegalStateException("Static id was blank: [" + staticId + "]");
                }
                yield staticId;
            }
        };
    }

    private static String hostname() {
        try {
            return Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException("Couldn't resolve hostname!", e);
        }
    }

}
