package com.aiposter.poster;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosterGenerationAssetRepository extends JpaRepository<PosterGenerationAssetEntity, Long> {
    List<PosterGenerationAssetEntity> findByTaskIdOrderBySortOrderAsc(Long taskId);
}
