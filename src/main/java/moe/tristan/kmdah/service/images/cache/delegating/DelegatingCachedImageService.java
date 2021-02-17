package moe.tristan.kmdah.service.images.cache.delegating;

import java.io.InputStream;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;

import moe.tristan.kmdah.service.images.ImageContent;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.images.cache.CachedImageService;
import moe.tristan.kmdah.service.images.cache.VacuumingRequest;
import moe.tristan.kmdah.service.images.cache.VacuumingResult;

public class DelegatingCachedImageService implements CachedImageService {

    @Override
    public Optional<ImageContent> findImage(ImageSpec imageSpec) {
        return Optional.empty();
    }

    @Override
    public void saveImage(ImageSpec imageSpec, MediaType mediaType, InputStream inputStream) {
    }

    @Override
    public void deleteChapter(ImageSpec imageSpec) {
    }

    @Override
    public VacuumingResult vacuum(VacuumingRequest vacuumingRequest) {
        return new VacuumingResult(0L, DataSize.ofBytes(0L));
    }

}
