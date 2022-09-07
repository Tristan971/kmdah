package moe.tristan.kmdah.mangadex;

import org.springframework.stereotype.Component;

@Component
public class MangadexApi {

    private final MangaDexSettings mangadexSettings;

    public MangadexApi(MangaDexSettings mangadexSettings) {
        this.mangadexSettings = mangadexSettings;
    }

    public String getApiUrl() {
        return mangadexSettings.overrideApiUrl().orElse("https://api.mangadex.network");
    }

}
