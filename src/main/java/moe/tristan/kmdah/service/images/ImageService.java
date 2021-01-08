package moe.tristan.kmdah.service.images;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bouncycastle.util.io.TeeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import moe.tristan.kmdah.mangadex.image.MangadexImageService;
import moe.tristan.kmdah.service.gossip.messages.LeaderImageServerEvent;
import moe.tristan.kmdah.service.images.cache.CacheMode;
import moe.tristan.kmdah.service.images.cache.CachedImageService;

@Service
public class ImageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newVirtualThreadExecutor();

    private final CachedImageService cachedImageService;
    private final MangadexImageService mangadexImageService;

    private String upstreamServerUri = "https://s2.mangadex.org";

    public ImageService(
        CachedImageService cachedImageService,
        MangadexImageService mangadexImageService
    ) {
        this.cachedImageService = cachedImageService;
        this.mangadexImageService = mangadexImageService;
    }

    public ImageContent findOrFetch(ImageSpec imageSpec) {
        return cachedImageService
            .findImage(imageSpec)
            .orElseGet(() -> fetchFromUpstream(imageSpec));
    }

    private ImageContent fetchFromUpstream(ImageSpec imageSpec) {
        ImageContent upstreamContent = mangadexImageService.download(imageSpec, upstreamServerUri);

        try {
            InputStream upstreamIs = upstreamContent.resource().getInputStream();

            PipedOutputStream responseOutputStreamPipe = new PipedOutputStream();
            PipedInputStream responseInputStream = new PipedInputStream(responseOutputStreamPipe);

            TeeInputStream cacheSaveInputStream = new TeeInputStream(upstreamIs, responseOutputStreamPipe);
            ImageContent cacheSaveContent = new ImageContent(
                new InputStreamResource(cacheSaveInputStream),
                upstreamContent.contentType(),
                upstreamContent.contentLength(),
                upstreamContent.lastModified(),
                CacheMode.MISS
            );

            CompletableFuture.runAsync(() -> {
                LOGGER.info("Starting cache committing");
                cachedImageService.saveImage(imageSpec, cacheSaveContent);
            }, EXECUTOR_SERVICE);

            return new ImageContent(
                new InputStreamResource(responseInputStream),
                upstreamContent.contentType(),
                upstreamContent.contentLength(),
                upstreamContent.lastModified(),
                CacheMode.MISS
            );
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @EventListener(LeaderImageServerEvent.class)
    public void onLeaderImageServerEvent(LeaderImageServerEvent leaderImageServerEvent) {
        if (!upstreamServerUri.equals(leaderImageServerEvent.imageServer())) {
            LOGGER.info("Changed upstream server uri to: {}", leaderImageServerEvent.imageServer());
            this.upstreamServerUri = leaderImageServerEvent.imageServer();
        }
    }

}
