package moe.tristan.kmdah.service.leader.vacuum;

import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import moe.tristan.kmdah.service.images.cache.CacheSettings;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;
import moe.tristan.kmdah.service.leader.LeaderActivity;

@Component
public class VacuumJob implements LeaderActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(VacuumJob.class);

    private final CacheSettings cacheSettings;
    private final CachedImageService cachedImageService;

    public VacuumJob(CacheSettings cacheSettings, CachedImageService cachedImageService) {
        this.cacheSettings = cacheSettings;
        this.cachedImageService = cachedImageService;
    }

    @Override
    public String getName() {
        return "Vacuum";
    }

    @Override
    public Duration getInitialDelay() {
        return Duration.ofSeconds(5);
    }

    @Override
    public Duration getPeriod() {
        return Duration.ofMinutes(15);
    }

    @Override
    public void run() {
        VacuumingRequest vacuumingRequest = new VacuumingRequest(DataSize.ofGigabytes(cacheSettings.maxSizeGb()));
        Optional<VacuumingResult> vacuumingResult = cachedImageService
            .vacuum(vacuumingRequest)
            .filter(result -> result.deletedFileCount() > 0)
            .blockOptional();

        if (vacuumingResult.isPresent()) {
            VacuumingResult result = vacuumingResult.get();
            LOGGER.info(
                "Vacuuming run done - freed {}MB by deleting {} files",
                result.freedSpace().toMegabytes(),
                result.deletedFileCount()
            );
        } else {
            LOGGER.info("Vacuuming run finished without any deletion");
        }
    }

}
