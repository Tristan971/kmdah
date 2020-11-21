package moe.tristan.kmdah.mangadex;

import org.springframework.stereotype.Component;

@Component
public class MangadexApi {

    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX";

    private final String apiUrl = "https://api.mangadex.network";

    public String getApiUrl() {
        return apiUrl;
    }

}
