package moe.tristan.kmdah.service.gossip.messages;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = NAME,
    include = EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @Type(value = WorkerPingEvent.class, name = "WORKER_PING"),
    @Type(value = WorkerShutdownEvent.class, name = "WORKER_SHUTDOWN"),
    @Type(value = LeaderImageServerEvent.class, name = "LEADER_IMAGE_SERVER"),
    @Type(value = LeaderTokenEvent.class, name = "LEADER_TOKEN")
})
public interface GossipMessage {

    @JsonProperty("type")
    GossipMessageType getType();

}
