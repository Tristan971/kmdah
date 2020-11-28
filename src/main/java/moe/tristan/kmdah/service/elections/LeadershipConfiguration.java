package moe.tristan.kmdah.service.elections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;

@Configuration
public class LeadershipConfiguration {

    @Bean
    RedisLockRegistry leadershipLockRegistry(RedisConnectionFactory redisConnectionFactory, LeadershipRedisSettings leadershipRedisSettings) {
        return new RedisLockRegistry(redisConnectionFactory, leadershipRedisSettings.lockRegistryKey(), 5000);
    }

    @Bean
    LockRegistryLeaderInitiator lockRegistryLeaderInitiator(RedisLockRegistry leadershipLockRegistry, LeadershipCandidate leadershipCandidate) {
        return new LockRegistryLeaderInitiator(leadershipLockRegistry, leadershipCandidate);
    }

}
