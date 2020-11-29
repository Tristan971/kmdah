package moe.tristan.kmdah.service.gossip.elections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.leader.AbstractCandidate;
import org.springframework.integration.leader.Context;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.service.gossip.InstanceId;

@Component
public class ElectionsCandidate extends AbstractCandidate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElectionsCandidate.class);

    private static final String LEADERSHIP_ROLE = "kmdah-leadership";

    private final ApplicationEventPublisher applicationEventPublisher;

    public ElectionsCandidate(InstanceId instanceId, ApplicationEventPublisher applicationEventPublisher) {
        super(instanceId.id(), LEADERSHIP_ROLE);
        this.applicationEventPublisher = applicationEventPublisher;
        LOGGER.info("Will participate in leadership elections for role [{}] as [{}]", getRole(), getId());
    }

    @Override
    public void onGranted(Context ctx) {
        LOGGER.info("Granted leader mandate for {}", ctx.getRole());
        applicationEventPublisher.publishEvent(new GrantedLeadershipEvent());
    }

    @Override
    public void onRevoked(Context ctx) {
        LOGGER.info("Leadership mandate for {} was revoked", ctx.getRole());
        applicationEventPublisher.publishEvent(new RevokedLeadershipEvent());
    }

}
