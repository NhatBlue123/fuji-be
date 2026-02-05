package com.example.fuji.repository;
//course repository để truy xuất dữ liệu khóa học từ database
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // Lấy tất cả khóa học đã publish với phân trang
    Page<Course> findByIsPublishedTrue(Pageable pageable);

    // Lấy khóa học theo instructor với phân trang
    Page<Course> findByInstructorId(Long instructorId, Pageable pageable);

    // Tìm kiếm khóa học theo tiêu đề với phân trang
    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND c.isPublished = true")
    Page<Course> searchByTitle(String keyword, Pageable pageable);
}
