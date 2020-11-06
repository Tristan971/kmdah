package moe.tristan.kmdah.worker.service.images;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import moe.tristan.kmdah.common.internal.model.image.ImageEntity;
import moe.tristan.kmdah.common.mangadex.image.ImageMode;

public interface CachedImagesRepository extends JpaRepository<ImageEntity, String> {

    Optional<ImageEntity> findByIdAndChapterIdAndMode(String id, String chapterId, ImageMode mode);

}
