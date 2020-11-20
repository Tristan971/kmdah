package moe.tristan.kmdah.cache;

import java.util.Optional;

import reactor.core.publisher.Mono;

import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;

public interface ImageCache {

    Mono<ImageContent> findImage(ImageSpec imageSpec);

    Mono<?> saveImage(ImageSpec imageSpec, ImageContent imageContent);

    Optional<VacuumingResult> vacuumIfNecessary(VacuumingRequest vacuumingRequest);

}
