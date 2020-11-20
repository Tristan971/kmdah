package moe.tristan.kmdah.service.vacuum;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import moe.tristan.kmdah.cache.ImageCache;
import moe.tristan.kmdah.cache.VacuumingRequest;

@Component
public class VacuumJob {

    private final ImageCache imageCache;

    public VacuumJob(ImageCache imageCache) {
        this.imageCache = imageCache;
    }

    // once per hour
    @Scheduled(fixedDelay = 3600 * 1000)
    public void execute() {
        VacuumingRequest req = null;
        imageCache.vacuumIfNecessary(req);
    }

}
