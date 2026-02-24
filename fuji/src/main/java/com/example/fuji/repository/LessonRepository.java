package com.example.fuji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByCourseIdOrderByLessonOrderAsc(Long courseId);

    Optional<Lesson> findByIdAndCourseId(Long id, Long courseId);

    int countByCourseId(Long courseId);

    @Modifying
    @Query("DELETE FROM Lesson l WHERE l.course.id = :courseId")
    void deleteByCourseId(Long courseId);

    @Query("SELECT COALESCE(MAX(l.lessonOrder), 0) FROM Lesson l WHERE l.course.id = :courseId")
    int findMaxLessonOrderByCourseId(Long courseId);
}
