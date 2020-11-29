package moe.tristan.kmdah.service.gossip.messages.sub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import moe.tristan.kmdah.service.gossip.RedisSettings;

@Configuration
public class GossipSubscriberConfiguration {

    @Bean
    RedisMessageListenerContainer gossipSubscriberListenerContainer(
        RedisSettings redisSettings,
        GossipSubscriber gossipSubscriber,
        RedisConnectionFactory redisConnectionFactory
    ) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(
            new MessageListenerAdapter(gossipSubscriber),
            ChannelTopic.of(redisSettings.gossipTopic())
        );
        return redisMessageListenerContainer;
    }

}
