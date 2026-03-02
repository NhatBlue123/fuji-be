package com.example.fuji.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

       // ── Eager-fetch instructor + createdBy to avoid LazyInitializationException ──

       @Query(value = "SELECT c FROM Course c JOIN FETCH c.instructor JOIN FETCH c.createdBy", countQuery = "SELECT COUNT(c) FROM Course c")
       Page<Course> findAllWithUsers(Pageable pageable);

       @Query(value = "SELECT c FROM Course c JOIN FETCH c.instructor JOIN FETCH c.createdBy WHERE c.isPublished = true", countQuery = "SELECT COUNT(c) FROM Course c WHERE c.isPublished = true")
       Page<Course> findByIsPublishedTrueWithUsers(Pageable pageable);

       @Query(value = "SELECT c FROM Course c JOIN FETCH c.instructor JOIN FETCH c.createdBy WHERE c.instructor.id = :instructorId", countQuery = "SELECT COUNT(c) FROM Course c WHERE c.instructor.id = :instructorId")
       Page<Course> findByInstructorIdWithUsers(@Param("instructorId") Long instructorId, Pageable pageable);

       @Query(value = "SELECT c FROM Course c JOIN FETCH c.instructor JOIN FETCH c.createdBy WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.isPublished = true", countQuery = "SELECT COUNT(c) FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.isPublished = true")
       Page<Course> searchByTitleWithUsers(@Param("keyword") String keyword, Pageable pageable);

       @Query("SELECT c FROM Course c JOIN FETCH c.instructor JOIN FETCH c.createdBy WHERE c.id = :id")
       Optional<Course> findByIdWithUsers(@Param("id") Long id);

       // ── Legacy (kept for backward compatibility) ──

       Page<Course> findByIsPublishedTrue(Pageable pageable);

       Page<Course> findByInstructorId(Long instructorId, Pageable pageable);

       @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.isPublished = true")
       Page<Course> searchByTitle(@Param("keyword") String keyword, Pageable pageable);
}
