package moe.tristan.kmdah.worker.service.mangadex;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import moe.tristan.kmdah.worker.model.ImageRequest;
import moe.tristan.kmdah.worker.service.lifecycle.WorkerConfigurationService;

import io.micrometer.core.annotation.Timed;

@Service
public class MangadexImageService {

    private final WorkerConfigurationService workerConfigurationService;
    private final RestTemplate restTemplate;

    public MangadexImageService(WorkerConfigurationService workerConfigurationService, RestTemplate restTemplate) {
        this.workerConfigurationService = workerConfigurationService;
        this.restTemplate = restTemplate;
    }

    @Timed
    public ResponseEntity<byte[]> download(ImageRequest imageRequest) {

        URI serverSideUri = UriComponentsBuilder
            .fromHttpUrl(workerConfigurationService.getImageServer())
            .path("{mode}/{chapter}/{file}")
            .build(
                imageRequest.getMode().getPathFragment(),
                imageRequest.getChapterHash(),
                imageRequest.getFilename()
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
