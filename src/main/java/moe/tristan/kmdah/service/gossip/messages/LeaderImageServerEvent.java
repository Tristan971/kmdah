package moe.tristan.kmdah.service.gossip.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LeaderImageServerEvent(

    @JsonProperty("imageServer")
    String imageServer

) implements GossipMessage {

    @Override
    public GossipMessageType getType() {
        return GossipMessageType.LEADER_IMAGE_SERVER;
    }

}
