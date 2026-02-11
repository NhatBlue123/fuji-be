package com.example.fuji.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByFlashCardIdOrderByCardOrderAsc(Long flashCardId);

    void deleteByFlashCardId(Long flashCardId);

    int countByFlashCardId(Long flashCardId);
}
