package com.example.fuji.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.FlashCard;
import com.example.fuji.enums.JlptLevel;

@Repository
public interface FlashCardRepository extends JpaRepository<FlashCard, Long> {

    Optional<FlashCard> findByIdAndDeletedAtIsNull(Long id);

    Page<FlashCard> findByIsPublicTrueAndDeletedAtIsNull(Pageable pageable);

    Page<FlashCard> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    @Query("SELECT fc FROM FlashCard fc WHERE fc.deletedAt IS NULL " +
           "AND (fc.isPublic = true OR fc.user.id = :userId)")
    Page<FlashCard> findAllAccessible(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT fc FROM FlashCard fc WHERE fc.deletedAt IS NULL " +
           "AND LOWER(fc.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "AND (:level IS NULL OR fc.level = :level) " +
           "AND (" +
           "   :select = 'all' AND (fc.isPublic = true OR fc.user.id = :userId) " +
           "   OR :select = 'me' AND fc.user.id = :userId " +
           "   OR :select = 'other' AND fc.isPublic = true AND fc.user.id != :userId" +
           ")")
    Page<FlashCard> search(
        @Param("query") String query,
        @Param("level") JlptLevel level,
        @Param("select") String select,
        @Param("userId") Long userId,
        Pageable pageable
    );

    void deleteByUserId(Long userId);
}
