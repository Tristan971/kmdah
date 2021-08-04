package moe.tristan.kmdah.mangadex.image;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.OptionalLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CacheMode;

@Service
public class MangadexImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MangadexImageService.class);

    private final RestTemplate restTemplate;

    public MangadexImageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ImageContent download(ImageSpec imageRequest, String upstreamServerUri) {
        URI uri = UriComponentsBuilder
            .fromHttpUrl(upstreamServerUri)
            .path("/{mode}/{chapter}/{file}")
            .buildAndExpand(imageRequest.mode().getPathFragment(), imageRequest.chapter(), imageRequest.file())
            .toUri();

        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(uri, byte[].class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MangadexUpstreamException("Upstream returned an error status code: " + response.getStatusCode());
            }

            if (response.getBody() == null || response.getBody().length == 0) {
                throw new MangadexUpstreamException("Upstream returned no body!");
            }

            MediaType contentType = Objects.requireNonNull(
                response.getHeaders().getContentType(),
                "Content-Type was not set by upstream for: " + uri
            );

            long contentLength = response.getHeaders().getContentLength();

            if (contentLength == 0) {
                throw new MangadexUpstreamException("Upstream returned a content length of 0!");
            }

            long upstreamLastModified = response.getHeaders().getLastModified();
            Instant lastModified = upstreamLastModified != -1
                ? Instant.ofEpochMilli(upstreamLastModified)
                : Instant.now();

            return new ImageContent(
                new InputStreamResource(new ByteArrayInputStream(response.getBody())),
                contentType,
                contentLength != -1 ? OptionalLong.of(contentLength) : OptionalLong.empty(),
                lastModified,
                CacheMode.MISS
            );
        } catch (RestClientException e) {
            throw new MangadexUpstreamException("Failed upstream fetch for " + imageRequest, e);
        }
    }

}
