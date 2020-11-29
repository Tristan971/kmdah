package moe.tristan.kmdah.service.images.cache;

import org.springframework.util.unit.DataSize;

public record VacuumingRequest(

    DataSize targetSize

) {}
