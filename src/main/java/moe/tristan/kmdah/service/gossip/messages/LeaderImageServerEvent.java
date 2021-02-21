package moe.tristan.kmdah.service.gossip.messages;

import static moe.tristan.kmdah.service.gossip.messages.GossipMessageType.LEADER_IMAGE_SERVER;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LeaderImageServerEvent(

    @JsonProperty("imageServer")
    String imageServer

) implements GossipMessage {

    @Override
    public GossipMessageType getType() {
        return LEADER_IMAGE_SERVER;
    }

}
