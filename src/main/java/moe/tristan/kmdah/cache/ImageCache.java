package moe.tristan.kmdah.cache;

import java.util.Optional;

import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;
import reactor.core.publisher.Mono;

public interface ImageCache {

    Mono<ImageContent> findImage(ImageSpec imageSpec);

    void saveImage(ImageSpec imageSpec, ImageContent imageContent);

    Optional<VacuumingResult> vacuumIfNecessary(VacuumingRequest vacuumingRequest);

}
