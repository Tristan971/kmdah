package moe.tristan.kmdah.service.images.cache;

import java.util.Optional;

import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;

public interface CachedImageService {

    Optional<ImageContent> findImage(ImageSpec imageSpec);

    void saveImage(ImageSpec imageSpec, ImageContent imageContent);

    VacuumingResult vacuum(VacuumingRequest vacuumingRequest);

}
