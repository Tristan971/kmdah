package moe.tristan.kmdah.service.sync;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.leader.LockRegistryLeaderInitiator;

import moe.tristan.kmdah.service.sync.leadership.LeadershipCandidate;
import moe.tristan.kmdah.service.sync.workers.WorkersRegistry;

@Configuration
public class SyncConfiguration {

    @Bean
    ChannelTopic channelTopic(RedisSettings redisSettings) {
        return ChannelTopic.of(redisSettings.topic());
    }

    @Bean
    MessageListenerAdapter workerEventsListener(WorkersRegistry workersRegistry) {
        return new MessageListenerAdapter(workersRegistry);
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(
        ChannelTopic channelTopic,
        MessageListenerAdapter messageListenerAdapter,
        RedisConnectionFactory redisConnectionFactory
    ) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(messageListenerAdapter, channelTopic);
        return redisMessageListenerContainer;
    }

    @Bean
    RedisLockRegistry leadershipLockRegistry(RedisConnectionFactory redisConnectionFactory, RedisSettings redisSettings) {
        return new RedisLockRegistry(redisConnectionFactory, redisSettings.lockRegistryKey(), 5000);
    }

    @Bean
    LockRegistryLeaderInitiator lockRegistryLeaderInitiator(RedisLockRegistry leadershipLockRegistry, LeadershipCandidate leadershipCandidate) {
        return new LockRegistryLeaderInitiator(leadershipLockRegistry, leadershipCandidate);
    }

}
