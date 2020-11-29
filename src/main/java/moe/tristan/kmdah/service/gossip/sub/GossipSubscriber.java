package moe.tristan.kmdah.service.gossip.sub;

import java.io.IOException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import moe.tristan.kmdah.service.gossip.GossipMessage;

/**
 * Forwards redis cluster-wide gossip events through the local Spring event bus
 */
@Component
public class GossipSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public GossipSubscriber(ObjectMapper objectMapper, ApplicationEventPublisher applicationEventPublisher) {
        this.objectMapper = objectMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            GossipMessage gossipMessage = objectMapper.readValue(message.getBody(), GossipMessage.class);
            applicationEventPublisher.publishEvent(gossipMessage);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read gossip message: " + new String(message.getBody()), e);
        }
    }

}
