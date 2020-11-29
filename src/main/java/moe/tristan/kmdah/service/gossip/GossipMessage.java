package moe.tristan.kmdah.service.gossip;

import com.fasterxml.jackson.annotation.JsonProperty;

import moe.tristan.kmdah.service.workers.WorkerInfo;

public record GossipMessage(

    @JsonProperty("worker")
    WorkerInfo worker,

    @JsonProperty("type")
    GossipMessageType type

) {

    public enum GossipMessageType {
        PING,
        SHUTDOWN
    }

}
