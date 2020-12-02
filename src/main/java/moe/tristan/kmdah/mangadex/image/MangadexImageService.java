package moe.tristan.kmdah.mangadex.image;

import static java.util.Objects.requireNonNull;

import java.util.OptionalLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;

@Service
public class MangadexImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MangadexImageService.class);

    private final WebClient webClient;

    public MangadexImageService(WebClient.Builder webClient) {
        this.webClient = webClient.build();
    }

    public Mono<ImageContent> download(ImageSpec imageRequest, String upstreamServerUri) {
        return webClient
            .get()
            .uri(upstreamServerUri + "/{mode}/{chapter}/{file}", imageRequest.mode().getPathFragment(), imageRequest.chapter(), imageRequest.file())
            .retrieve()
            .toEntityFlux(DataBuffer.class)
            .map(entityFlux -> {
                if (entityFlux.getStatusCode().is4xxClientError() || entityFlux.getStatusCode().is5xxServerError()) {
                    throw new MangadexUpstreamException("Upstream returned an error status code: " + entityFlux.getStatusCode());
                }

                MediaType contentType = entityFlux.getHeaders().getContentType();
                long contentLength = entityFlux.getHeaders().getContentLength();

                return new ImageContent(
                    requireNonNull(entityFlux.getBody(), "Empty upstream response!"),
                    contentType,
                    contentLength != -1 ? OptionalLong.of(contentLength) : OptionalLong.empty(),
                    CacheMode.MISS
                );
            })
            .doOnSuccess(content -> LOGGER.info("Retrieved {} from upstream {} as {}", imageRequest, upstreamServerUri, content))
            .doOnError(error -> LOGGER.error("Failed to retrieve {} from upstream {} due to an upstream error.", imageRequest, upstreamServerUri, error));
    }

}
