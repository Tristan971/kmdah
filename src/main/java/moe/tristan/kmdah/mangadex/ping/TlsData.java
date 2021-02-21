package moe.tristan.kmdah.mangadex.ping;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TlsData(

    @JsonProperty("created_at")
    ZonedDateTime createdAt,

    @JsonProperty("private_key")
    String privateKey,

    @JsonProperty("certificate")
    String certificate

) {

    @Override
    public String toString() {
        return "TlsData[" +
            "createdAt=" + createdAt +
            ", privateKey=*** (" + privateKey.length() + ")" +
            ", certificate=*** (" + certificate.length() + ")" +
            ']';
    }

}
