package com.aiposter.asset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<AssetEntity, Long> {
    List<AssetEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
