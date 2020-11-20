package moe.tristan.kmdah.cache.mongodb;

import java.util.Optional;

import org.springframework.stereotype.Component;

import moe.tristan.kmdah.cache.ImageCache;
import moe.tristan.kmdah.cache.VacuumingRequest;
import moe.tristan.kmdah.cache.VacuumingResult;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;

@Component
public class MongodbImageCache implements ImageCache {
    @Override
    public Optional<ImageContent> findImage(ImageSpec imageSpec) {
        return Optional.empty();
    }

    @Override
    public void saveImage(ImageSpec imageSpec, ImageContent imageContent) {

    }

    @Override
    public Optional<VacuumingResult> vacuumIfNecessary(VacuumingRequest vacuumingRequest) {
        return Optional.empty();
    }
}
