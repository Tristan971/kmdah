package moe.tristan.kmdah.worker.service.images;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import moe.tristan.kmdah.common.model.persistence.ImageEntity;
import moe.tristan.kmdah.common.model.mangadex.image.ImageMode;

public interface ImagesRepository extends JpaRepository<ImageEntity, String> {

    Optional<ImageEntity> findByFilenameAndChapterHashAndMode(String filename, String chapterHash, ImageMode mode);

}
