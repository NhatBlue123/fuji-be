package com.example.fuji.entity;


import java.math.BigDecimal;

import com.example.fuji.entity.enums.JLPTLevel;
import com.example.fuji.entity.enums.TestType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "jlpt_tests")
@Data
public class JlptTest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private JLPTLevel level;

    @Enumerated(EnumType.STRING)
    private TestType testType;

    private Integer duration; // Phút
    private Integer totalQuestions;
    private BigDecimal passingScore;

    @Column(name = "is_published")
    private Boolean isPublished = false;
}
