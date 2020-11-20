package moe.tristan.kmdah.mangadex.image;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class MangadexHeaders {

    public void addHeaders(HttpHeaders headers) {
        // MDAH spec headers
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://mangadex.org");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
        headers.add(HttpHeaders.CACHE_CONTROL, "public/ max-age=1209600");
        headers.add("Timing-Allow-Origin", "https://mangadex.org");
        headers.add("X-Content-Type-Options", "nosniff");
    }

}
