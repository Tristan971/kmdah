package moe.tristan.kmdah.service.leader.vacuum;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Mono;

import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;
import moe.tristan.kmdah.service.images.cache.CacheSettings;

@SpringBootTest(
    classes = VacuumJob.class,
    properties = "kmdah.cache.max-size-gb=100"
)
@EnableConfigurationProperties(CacheSettings.class)
class VacuumJobTest {

    @MockBean
    private CachedImageService cachedImageService;

    @Autowired
    private CacheSettings cacheSettings;

    @Autowired
    private VacuumJob vacuumJob;

    @Test
    void vacuumingTest() {
        VacuumingRequest expectedVacuumingRequest = new VacuumingRequest(DataSize.ofGigabytes(cacheSettings.maxSizeGb()));

        when(cachedImageService.vacuum(eq(expectedVacuumingRequest)))
            .thenReturn(Mono.just(new VacuumingResult(5L, DataSize.ofMegabytes(10))));

        vacuumJob.run();

        verify(cachedImageService).vacuum(eq(expectedVacuumingRequest));
    }

}
