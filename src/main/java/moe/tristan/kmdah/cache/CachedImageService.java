package moe.tristan.kmdah.cache;

import reactor.core.publisher.Mono;

import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;

public interface CachedImageService {

    Mono<ImageContent> findImage(ImageSpec imageSpec);

    Mono<?> saveImage(ImageSpec imageSpec, ImageContent imageContent);

    VacuumingResult vacuum(VacuumingRequest vacuumingRequest);

}
