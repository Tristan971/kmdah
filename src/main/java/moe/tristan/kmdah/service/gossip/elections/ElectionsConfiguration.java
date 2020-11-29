package moe.tristan.kmdah.service.gossip.elections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;

import moe.tristan.kmdah.service.gossip.RedisSettings;

@Configuration
public class ElectionsConfiguration {

    @Bean
    RedisLockRegistry leadershipLockRegistry(RedisConnectionFactory redisConnectionFactory, RedisSettings redisSettings) {
        return new RedisLockRegistry(redisConnectionFactory, redisSettings.lockRegistryKey(), 5000);
    }

    @Bean
    LockRegistryLeaderInitiator lockRegistryLeaderInitiator(RedisLockRegistry leadershipLockRegistry, ElectionsCandidate electionsCandidate) {
        return new LockRegistryLeaderInitiator(leadershipLockRegistry, electionsCandidate);
    }

}
