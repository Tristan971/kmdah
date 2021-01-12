package moe.tristan.kmdah.service.metrics;

import static java.lang.System.nanoTime;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import moe.tristan.kmdah.service.images.cache.CacheMode;

@Component
public class ImageMetrics {

    private static final String METRICS_PREFIX = "kmdah_image_";

    private static final String OP_SERVE_IMAGE = METRICS_PREFIX + "serve";

    private static final String OP_SEARCH_FROM_CACHE = METRICS_PREFIX + "search_from_cache";
    private static final String OP_SEARCH_FROM_UPSTREAM = METRICS_PREFIX + "search_from_upstream";
    private static final String OP_SEARCH_IMAGE = METRICS_PREFIX + "search";

    private static final String CACHE_MODE_TAG_KEY = "cache_mode";
    private static final String FOUND_TAG_KEY = "found";

    private final MeterRegistry meterRegistry;

    public ImageMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordSearch(long start, CacheMode cacheMode) {
        meterRegistry.timer(
            OP_SEARCH_IMAGE,
            CACHE_MODE_TAG_KEY, cacheMode.name()
        ).record(nanoTime() - start, TimeUnit.NANOSECONDS);
    }

    public void recordSearchFromCache(long start, boolean found) {
        meterRegistry.timer(
            OP_SEARCH_FROM_CACHE,
            FOUND_TAG_KEY, String.valueOf(found)
        ).record(nanoTime() - start, TimeUnit.NANOSECONDS);
    }

    public void recordSearchFromUpstream(long start) {
        meterRegistry
            .timer(OP_SEARCH_FROM_UPSTREAM)
            .record(nanoTime() - start, TimeUnit.NANOSECONDS);
    }

    public void recordServe(long start, CacheMode cacheMode) {
        meterRegistry.timer(
            OP_SERVE_IMAGE,
            CACHE_MODE_TAG_KEY, cacheMode.name()
        ).record(nanoTime() - start, TimeUnit.NANOSECONDS);
    }

}
