package moe.tristan.kmdah.mangadex.image;

import java.awt.image.DataBuffer;
import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.cache.CacheMode;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;
import reactor.core.publisher.Mono;

@Service
public class MangadexImageService {

    private final WebClient webClient;

    public MangadexImageService(WebClient.Builder webClient) {
        this.webClient = webClient.build();
    }

    public Mono<ImageContent> download(ImageSpec imageRequest) {

        URI upstreamUri = UriComponentsBuilder
            .fromHttpUrl("https://tbd")
            .path("{mode}/{chapter}/{file}")
            .build(
                imageRequest.mode().getPathFragment(),
                imageRequest.chapter(),
                imageRequest.file()
            );

        return webClient
            .get()
            .uri(upstreamUri)
            .exchangeToMono(response -> Mono.just(
                new ImageContent(
                    response.bodyToFlux(DataBuffer.class),
                    response.headers().contentType(),
                    response.headers().contentLength(),
                    CacheMode.MISS
                ))
            );
    }

}
