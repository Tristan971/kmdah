package moe.tristan.kmdah.service.images.cache;

import org.springframework.util.unit.DataSize;

public record VacuumingResult(
    long deletedCount,
    DataSize freedSpace,
    VacuumGranularity granularity
) {

    public enum VacuumGranularity {
        FILE,
        CHAPTER
    }

}
