package moe.tristan.kmdah.service.sync.workers;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WorkerEvent(

    @JsonProperty("id")
    String id,

    @JsonProperty("type")
    WorkerEventType type

) {}
