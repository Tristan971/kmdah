package moe.tristan.kmdah.service.metrics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import moe.tristan.kmdah.service.images.cache.CacheMode;

@Component
public class CacheModeCounter implements MeterBinder {

    private static final String METRIC_NAME = "kmdah_cache_mode";

    private final Map<CacheMode, Counter> cacheModeCounters = new HashMap<>(CacheMode.values().length);

    public void record(CacheMode mode) {
        cacheModeCounters.get(mode).increment();
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Arrays.stream(CacheMode.values()).forEach(mode -> {
            Counter counter = registry.counter(
                METRIC_NAME,
                "mode", mode.name()
            );
            cacheModeCounters.put(mode, counter);
        });
    }

}
