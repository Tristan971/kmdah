package moe.tristan.kmdah.service.workers;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WorkerInfo(

    @JsonProperty("id")
    String id

) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorkerInfo that = (WorkerInfo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
