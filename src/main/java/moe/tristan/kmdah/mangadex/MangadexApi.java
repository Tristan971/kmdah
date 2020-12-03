package moe.tristan.kmdah.mangadex;

import org.springframework.stereotype.Component;

@Component
public class MangadexApi {

    public static final int SPEC_VERSION = 19;

    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX";

    public String getApiUrl() {
        return "https://api.mangadex.network";
    }

}
