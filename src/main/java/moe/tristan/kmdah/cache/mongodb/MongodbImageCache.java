package moe.tristan.kmdah.cache.mongodb;

import java.util.Optional;

import org.springframework.stereotype.Component;

import moe.tristan.kmdah.cache.ImageCache;
import moe.tristan.kmdah.cache.VacuumingRequest;
import moe.tristan.kmdah.cache.VacuumingResult;
import moe.tristan.kmdah.model.ImageContent;
import moe.tristan.kmdah.model.ImageSpec;
import reactor.core.publisher.Mono;

@Component
public class MongodbImageCache implements ImageCache {

    @Override
    public Mono<ImageContent> findImage(ImageSpec imageSpec) {
        return Mono.empty();
    }

    @Override
    public void saveImage(ImageSpec imageSpec, ImageContent imageContent) {

    }

    @Override
    public Optional<VacuumingResult> vacuumIfNecessary(VacuumingRequest vacuumingRequest) {
        return Optional.empty();
    }

}
