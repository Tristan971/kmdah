package moe.tristan.kmdah.service.gossip.elections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.leader.AbstractCandidate;
import org.springframework.integration.leader.Context;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.service.gossip.InstanceId;

@Component
public class ElectionsCandidate extends AbstractCandidate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElectionsCandidate.class);

    private static final String LEADERSHIP_ROLE = "kmdah-leadership";

    public ElectionsCandidate(InstanceId instanceId) {
        super(instanceId.id(), LEADERSHIP_ROLE);
        LOGGER.info("Will participate in leadership elections for role [{}] as [{}]", getRole(), getId());
    }

    @Override
    public void onGranted(Context ctx) throws InterruptedException {
        LOGGER.info("Granted leader mandate for {}", ctx.getRole());
    }

    @Override
    public void onRevoked(Context ctx) {
        LOGGER.info("Leadership mandate for {} was revoked", ctx.getRole());
    }

}
