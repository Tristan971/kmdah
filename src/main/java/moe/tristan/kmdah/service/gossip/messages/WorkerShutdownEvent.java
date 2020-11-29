package moe.tristan.kmdah.service.gossip.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

import moe.tristan.kmdah.service.workers.WorkerInfo;

public record WorkerShutdownEvent(

    @JsonProperty("worker")
    WorkerInfo worker

) implements GossipMessage {

    @Override
    public GossipMessageType getType() {
        return GossipMessageType.WORKER_SHUTDOWN;
    }

}
