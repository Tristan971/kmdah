package moe.tristan.kmdah.operator.service.vacuum;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import moe.tristan.kmdah.common.model.persistence.ImageEntity;

public interface ImageRepository extends JpaRepository<ImageEntity, String> {

    List<ImageEntity> findTop100By();

}
