package com.example.fuji.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.UserLessonCompletion;

@Repository
public interface UserLessonCompletionRepository extends JpaRepository<UserLessonCompletion, Long> {

    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);

    int countByUserIdAndCourseId(Long userId, Long courseId);
}
