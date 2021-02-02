package moe.tristan.kmdah.service.gossip.messages.pub;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.service.gossip.InstanceId;
import moe.tristan.kmdah.service.gossip.RedisSettings;
import moe.tristan.kmdah.service.gossip.messages.GossipMessage;
import moe.tristan.kmdah.service.gossip.messages.LeaderImageServerEvent;
import moe.tristan.kmdah.service.gossip.messages.WorkerPingEvent;
import moe.tristan.kmdah.service.gossip.messages.WorkerShutdownEvent;
import moe.tristan.kmdah.service.workers.WorkerInfo;

@Component
public class GossipPublisher {

    private final InstanceId instanceId;
    private final RedisSettings redisSettings;
    private final RedisTemplate<String, GossipMessage> workerEventsRedisTemplate;

    public GossipPublisher(
        InstanceId instanceId,
        RedisSettings redisSettings,
        RedisTemplate<String, GossipMessage> workerEventsRedisTemplate
    ) {
        this.instanceId = instanceId;
        this.redisSettings = redisSettings;
        this.workerEventsRedisTemplate = workerEventsRedisTemplate;
    }

    public void broadcastPing() {
        workerEventsRedisTemplate.convertAndSend(
            redisSettings.gossipTopic(),
            new WorkerPingEvent(new WorkerInfo(instanceId.id()))
        );
    }

    public void broadcastShutdown() {
        workerEventsRedisTemplate.convertAndSend(
            redisSettings.gossipTopic(),
            new WorkerShutdownEvent(new WorkerInfo(instanceId.id()))
        );
    }

    public void broadcastImageServer(String imageServer) {
        workerEventsRedisTemplate.convertAndSend(
            redisSettings.gossipTopic(),
            new LeaderImageServerEvent(imageServer)
        );
    }

}
