package moe.tristan.kmdah.service.gossip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InstanceIdConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceIdConfiguration.class);

    @Bean
    InstanceId instanceId(GossipSettings gossipSettings) {
        String id = InstanceId.generateId(gossipSettings.idGenerationStrategy());
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Instance id [" + id + "] is not valid!");
        }

        LOGGER.info("Assigned id [{}] to this instance by [{}] strategy", id, gossipSettings.idGenerationStrategy());
        return new InstanceId(id);
    }

}
