package moe.tristan.kmdah.service.gossip;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GossipMessage(

    @JsonProperty("instanceId")
    String instanceId,

    @JsonProperty("type")
    GossipMessageType type

) {

    public enum GossipMessageType {
        PING,
        SHUTDOWN
    }

}
