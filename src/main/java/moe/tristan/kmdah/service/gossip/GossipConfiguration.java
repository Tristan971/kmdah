package moe.tristan.kmdah.service.gossip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GossipConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GossipConfiguration.class);

    @Bean
    InstanceId instanceId(GossipSettings gossipSettings) {
        String id = InstanceId.generateId(gossipSettings.idGenerationStrategy());
        LOGGER.info("Assigned id {} to this instance by {} strategy", id, gossipSettings.idGenerationStrategy());
        return new InstanceId(id);
    }

}
