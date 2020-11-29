package moe.tristan.kmdah.service.gossip.messages;

import static moe.tristan.kmdah.service.gossip.messages.GossipMessageType.WORKER_PING;

import com.fasterxml.jackson.annotation.JsonProperty;

import moe.tristan.kmdah.service.workers.WorkerInfo;

public record WorkerPingEvent(

    @JsonProperty("worker")
    WorkerInfo worker

) implements GossipMessage {

    @Override
    public GossipMessageType getType() {
        return WORKER_PING;
    }

}
