package moe.tristan.kmdah.mangadex;

import org.springframework.stereotype.Component;

@Component
public class MangadexApi {

    public String getApiUrl() {
        return "https://api.mangadex.network";
    }

}
