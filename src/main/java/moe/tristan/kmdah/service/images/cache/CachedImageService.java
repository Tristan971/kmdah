package moe.tristan.kmdah.service.images.cache;

import java.io.InputStream;
import java.util.Optional;

import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;

public interface CachedImageService {

    Optional<ImageContent> findImage(ImageSpec imageSpec);

    void saveImage(ImageSpec imageSpec, InputStream inputStream);

    VacuumingResult vacuum(VacuumingRequest vacuumingRequest);

}
