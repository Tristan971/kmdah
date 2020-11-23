package moe.tristan.kmdah.mangadex.stop;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StopRequest(

    @JsonProperty("secret")
    String secret

) {

    @Override
    public String toString() {
        //noinspection SuspiciousRegexArgument
        return "StopRequest[" +
            "secret=" + secret.replaceAll(".", "*") +
            ']';
    }

}
