package moe.tristan.kmdah.worker.service.mangadex;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.worker.model.ImageRequest;

import io.micrometer.core.annotation.Timed;

@Service
public class MangadexImageService {

    private final RestTemplate restTemplate;

    public MangadexImageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Timed
    public ResponseEntity<byte[]> download(String imageServer, ImageRequest imageRequest) {

        URI serverSideUri = UriComponentsBuilder
            .fromHttpUrl(imageServer)
            .path("{mode}/{chapter}/{file}")
            .build(
                imageRequest.getMode().getPathFragment(),
                imageRequest.getChapter(),
                imageRequest.getFile()
            );

        ResponseEntity<byte[]> response;
        try {
            response = restTemplate.getForEntity(serverSideUri, byte[].class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch image from upstream: " + serverSideUri, e);
        }

        if (!HttpStatus.OK.equals(response.getStatusCode())) {
            throw new HttpServerErrorException(response.getStatusCode(), "Upstream server returned non-200 answer!");
        }

        if (response.getBody() == null) {
            throw new IllegalStateException("Upstream returned an empty body for " + imageRequest);
        }

        return response;
    }

}
