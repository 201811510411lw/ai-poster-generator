package com.aiposter.poster;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosterTaskRepository extends JpaRepository<PosterTaskEntity, Long> {
    List<PosterTaskEntity> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
