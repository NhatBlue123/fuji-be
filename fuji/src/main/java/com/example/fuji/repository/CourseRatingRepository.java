package com.example.fuji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.CourseRating;

@Repository
public interface CourseRatingRepository extends JpaRepository<CourseRating, Long> {

    Optional<CourseRating> findByUserIdAndCourseId(Long userId, Long courseId);

    List<CourseRating> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    @Query("SELECT AVG(r.rating) FROM CourseRating r WHERE r.course.id = :courseId")
    Double getAverageRatingByCourseId(Long courseId);

    int countByCourseId(Long courseId);

    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
