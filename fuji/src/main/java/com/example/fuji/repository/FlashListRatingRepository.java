package com.example.fuji.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.FlashListRating;

@Repository
public interface FlashListRatingRepository extends JpaRepository<FlashListRating, Long> {

    Optional<FlashListRating> findByUserIdAndListId(Long userId, Long listId);

    @Query("SELECT AVG(r.rating) FROM FlashListRating r WHERE r.list.id = :listId")
    BigDecimal getAverageRatingByListId(@Param("listId") Long listId);

    @Query("SELECT COUNT(r) FROM FlashListRating r WHERE r.list.id = :listId")
    Integer countByListId(@Param("listId") Long listId);
}
