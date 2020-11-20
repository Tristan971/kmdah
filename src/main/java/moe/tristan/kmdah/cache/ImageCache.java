package moe.tristan.kmdah.cache;

import java.util.Optional;

import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;

public interface ImageCache {

    Optional<ImageContent> findImage(ImageSpec imageSpec);

    void saveImage(ImageSpec imageSpec, ImageContent imageContent);

    Optional<VacuumingResult> vacuumIfNecessary(VacuumingRequest vacuumingRequest);

}
