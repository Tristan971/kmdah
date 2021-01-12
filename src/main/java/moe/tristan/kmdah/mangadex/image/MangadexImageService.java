package moe.tristan.kmdah.mangadex.image;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.OptionalLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;

@Service
public class MangadexImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MangadexImageService.class);

    private final SimpleClientHttpRequestFactory httpRequestFactory;

    public MangadexImageService(SimpleClientHttpRequestFactory httpRequestFactory) {
        this.httpRequestFactory = httpRequestFactory;
    }

    public ImageContent download(ImageSpec imageRequest, String upstreamServerUri) {
        URI uri = UriComponentsBuilder
            .fromHttpUrl(upstreamServerUri)
            .path("/{mode}/{chapter}/{file}")
            .buildAndExpand(imageRequest.mode().getPathFragment(), imageRequest.chapter(), imageRequest.file())
            .toUri();

        try {
            ClientHttpRequest request = httpRequestFactory.createRequest(uri, HttpMethod.GET);
            ClientHttpResponse response = request.execute();

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MangadexUpstreamException("Upstream returned an error status code: " + response.getStatusCode());
            }
            LOGGER.info("Retrieving {} from upstream {}", imageRequest, upstreamServerUri);

            MediaType contentType = Objects.requireNonNull(
                response.getHeaders().getContentType(),
                "Content-Type was not set by upstream for: " + uri
            );

            long contentLength = response.getHeaders().getContentLength();

            long upstreamLastModified = response.getHeaders().getLastModified();
            Instant lastModified = upstreamLastModified != -1
                ? Instant.ofEpochMilli(upstreamLastModified)
                : Instant.now();

            return new ImageContent(
                new InputStreamResource(response.getBody()),
                contentType,
                contentLength != -1 ? OptionalLong.of(contentLength) : OptionalLong.empty(),
                lastModified,
                CacheMode.MISS
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
