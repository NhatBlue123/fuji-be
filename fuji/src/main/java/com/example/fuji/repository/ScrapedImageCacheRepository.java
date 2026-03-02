package com.example.fuji.repository;

import com.example.fuji.entity.ScrapedImageCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScrapedImageCacheRepository extends JpaRepository<ScrapedImageCache, Long> {

    Optional<ScrapedImageCache> findBySourceUrlHash(String sourceUrlHash);

    List<ScrapedImageCache> findBySourceUrlHashIn(List<String> sourceUrlHashes);
}
