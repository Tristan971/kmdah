package moe.tristan.kmdah.service.gossip.pub;

import static moe.tristan.kmdah.service.gossip.GossipMessage.GossipMessageType;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.service.gossip.GossipMessage;
import moe.tristan.kmdah.service.gossip.InstanceId;
import moe.tristan.kmdah.service.gossip.RedisSettings;

@Component
public class GossipPublisher {

    private final InstanceId instanceId;
    private final RedisSettings redisSettings;
    private final RedisTemplate<String, GossipMessage> workerEventsRedisTemplate;

    public GossipPublisher(InstanceId instanceId, RedisSettings redisSettings, RedisTemplate<String, GossipMessage> workerEventsRedisTemplate) {
        this.instanceId = instanceId;
        this.redisSettings = redisSettings;
        this.workerEventsRedisTemplate = workerEventsRedisTemplate;
    }

    public void broadcastPing() {
        workerEventsRedisTemplate.convertAndSend(redisSettings.topic(), new GossipMessage(instanceId.id(), GossipMessageType.PING));
    }

    public void broadcastShutdown() {
        workerEventsRedisTemplate.convertAndSend(redisSettings.topic(), new GossipMessage(instanceId.id(), GossipMessageType.SHUTDOWN));
    }

}
