package moe.tristan.kmdah.service.images.cache;

import reactor.core.publisher.Mono;

import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;

public interface CachedImageService {

    Mono<ImageContent> findImage(ImageSpec imageSpec);

    Mono<?> saveImage(ImageSpec imageSpec, ImageContent imageContent);

    Mono<VacuumingResult> vacuum(VacuumingRequest vacuumingRequest);

}
