package com.example.fuji.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.FlashList;
import com.example.fuji.enums.JlptLevel;

@Repository
public interface FlashListRepository extends JpaRepository<FlashList, Long> {

    Optional<FlashList> findByIdAndDeletedAtIsNull(Long id);

    Page<FlashList> findByIsPublicTrueAndDeletedAtIsNull(Pageable pageable);

    Page<FlashList> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    @Query("SELECT fl FROM FlashList fl WHERE fl.deletedAt IS NULL " +
           "AND (fl.isPublic = true OR fl.user.id = :userId)")
    Page<FlashList> findAllAccessible(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT fl FROM FlashList fl WHERE fl.deletedAt IS NULL " +
           "AND LOWER(fl.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "AND (:level IS NULL OR fl.level = :level) " +
           "AND (" +
           "   :select = 'all' AND (fl.isPublic = true OR fl.user.id = :userId) " +
           "   OR :select = 'me' AND fl.user.id = :userId " +
           "   OR :select = 'other' AND fl.isPublic = true AND fl.user.id != :userId" +
           ")")
    Page<FlashList> search(
        @Param("query") String query,
        @Param("level") JlptLevel level,
        @Param("select") String select,
        @Param("userId") Long userId,
        Pageable pageable
    );

    void deleteByUserId(Long userId);

    long countByUserId(Long userId);
}
