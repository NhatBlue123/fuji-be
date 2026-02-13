package com.example.fuji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.FlashListCard;

@Repository
public interface FlashListCardRepository extends JpaRepository<FlashListCard, Long> {

    Optional<FlashListCard> findByFlashListIdAndFlashCardId(Long listId, Long flashCardId);

    List<FlashListCard> findByFlashListIdOrderByCardOrderAsc(Long listId);

    List<FlashListCard> findByFlashCardId(Long flashCardId);

    void deleteByFlashListIdAndFlashCardId(Long listId, Long flashCardId);

    void deleteByFlashCardId(Long flashCardId);

    boolean existsByFlashListIdAndFlashCardId(Long listId, Long flashCardId);

    int countByFlashListId(Long listId);
}
