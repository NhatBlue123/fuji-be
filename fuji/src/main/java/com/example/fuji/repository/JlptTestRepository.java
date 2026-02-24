package com.example.fuji.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.JlptTest;
import com.example.fuji.entity.enums.JLPTLevel;
import com.example.fuji.entity.enums.TestType;

/**
 * Repository để truy vấn bảng jlpt_tests
 */
@Repository
public interface JlptTestRepository extends JpaRepository<JlptTest, Long> {

    /**
     * Tìm tất cả đề thi theo level JLPT
     * 
     * @param level    Mức độ JLPT (N5, N4, N3, N2, N1)
     * @param pageable Phân trang
     * @return Page chứa danh sách đề thi
     */
    Page<JlptTest> findByLevel(JLPTLevel level, Pageable pageable);

    /**
     * Tìm tất cả đề thi theo loại đề thi
     * 
     * @param testType Loại đề thi
     * @param pageable Phân trang
     * @return Page chứa danh sách đề thi
     */
    Page<JlptTest> findByTestType(TestType testType, Pageable pageable);

    /**
     * Tìm tất cả đề thi đã publish
     * 
     * @param pageable Phân trang
     * @return Page chứa danh sách đề thi đã publish
     */
    Page<JlptTest> findByIsPublishedTrue(Pageable pageable);

    /**
     * Tìm đề thi theo level và testType
     * 
     * @param level    Mức độ JLPT
     * @param testType Loại đề thi
     * @param pageable Phân trang
     * @return Page chứa danh sách đề thi
     */
    Page<JlptTest> findByLevelAndTestType(JLPTLevel level, TestType testType, Pageable pageable);

    /**
     * Tìm kiếm đề thi theo tiêu đề (có chứa keyword)
     * 
     * @param keyword  Từ khóa tìm kiếm
     * @param pageable Phân trang
     * @return Page chứa danh sách đề thi
     */
    @Query("SELECT t FROM JlptTest t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<JlptTest> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm đề thi đã publish theo level
     * 
     * @param level    Level JLPT
     * @param pageable Phân trang
     * @return Page chứa danh sách đề thi
     */
    @Query("SELECT t FROM JlptTest t WHERE t.isPublished = true " +
            "AND (:level IS NULL OR t.level = :level) " +
            "AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JlptTest> findPublishedByFilter(
            @Param("level") JLPTLevel level,
            @Param("keyword") String keyword,
            Pageable pageable);
}
