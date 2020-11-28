package moe.tristan.kmdah.service.sync.leadership;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.leader.AbstractCandidate;
import org.springframework.integration.leader.Context;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.service.sync.InstanceIdSettings;

@Component
public class LeadershipCandidate extends AbstractCandidate {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeadershipCandidate.class);

    private static final String LEADERSHIP_ROLE = "kmdah-leadership";

    public LeadershipCandidate(InstanceIdSettings instanceIdSettings) {
        super(instanceIdSettings.getId(), LEADERSHIP_ROLE);
        LOGGER.info("Will participate in leadership elections for role [{}] as [{}]", getRole(), getId());
    }

    @Override
    public void onGranted(Context ctx) throws InterruptedException {
        LOGGER.info("Was elected leader for {}", ctx.getRole());
    }

    @Override
    public void onRevoked(Context ctx) {
        LOGGER.info("Leadership mandate for {} was revoked. No longer elected.", ctx.getRole());
    }

}
