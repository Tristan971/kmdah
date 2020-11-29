package moe.tristan.kmdah.service.gossip.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(
    use = Id.NAME,
    include = As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @Type(value = WorkerPingEvent.class, name = "WORKER_PING"),
    @Type(value = WorkerShutdownEvent.class, name = "WORKER_SHUTDOWN"),
    @Type(value = LeaderImageServerEvent.class, name = "LEADER_IMAGE_SERVER")
})
public interface GossipMessage {

    @JsonProperty("type")
    GossipMessageType getType();

}
