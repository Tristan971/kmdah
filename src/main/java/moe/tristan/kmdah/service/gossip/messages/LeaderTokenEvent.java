package moe.tristan.kmdah.service.gossip.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LeaderTokenEvent(

    @JsonProperty("tokenKey")
    String tokenKey

) implements GossipMessage {

    @Override
    public GossipMessageType getType() {
        return GossipMessageType.LEADER_TOKEN;
    }

}
