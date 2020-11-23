package moe.tristan.kmdah.cache;

import org.springframework.util.unit.DataSize;

public record VacuumingResult(
    long deletedFileCount,
    DataSize freedSpace
) {}
