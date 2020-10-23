package moe.tristan.kmdah.operator.services.mangadex;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PingService {

    private final RestTemplate restTemplate;

    public PingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

}
