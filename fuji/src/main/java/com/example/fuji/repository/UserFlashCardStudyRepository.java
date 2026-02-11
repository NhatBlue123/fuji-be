package com.example.fuji.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.UserFlashCardStudy;

@Repository
public interface UserFlashCardStudyRepository extends JpaRepository<UserFlashCardStudy, Long> {

    Optional<UserFlashCardStudy> findByUserIdAndFlashCardId(Long userId, Long flashCardId);

    boolean existsByUserIdAndFlashCardId(Long userId, Long flashCardId);

    @Query("SELECT COUNT(ufcs) FROM UserFlashCardStudy ufcs WHERE ufcs.flashCard.id = :flashCardId")
    Long countByFlashCardId(@Param("flashCardId") Long flashCardId);

    @Query("SELECT COUNT(ufcs) FROM UserFlashCardStudy ufcs WHERE ufcs.flashCard.id = :flashCardId AND ufcs.isCompleted = true")
    Long countCompletedByFlashCardId(@Param("flashCardId") Long flashCardId);
}
