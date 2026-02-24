package com.example.fuji.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.fuji.entity.JlptQuestion;
import com.example.fuji.entity.enums.SectionType;

/**
 * Repository để truy vấn bảng jlpt_questions
 * 
 * Hỗ trợ cấu trúc parent-child và mondai
 */
@Repository
public interface JlptQuestionRepository extends JpaRepository<JlptQuestion, Long> {

    /**
     * Tìm tất cả câu hỏi của một đề thi, sắp xếp theo questionOrder
     * 
     * @param testId ID của đề thi
     * @return Danh sách câu hỏi đã sắp xếp
     */
    @Query("SELECT q FROM JlptQuestion q WHERE q.test.id = :testId ORDER BY q.questionOrder ASC")
    List<JlptQuestion> findByTestIdOrderByQuestionOrder(@Param("testId") Long testId);

    /**
     * Tìm tất cả câu hỏi PARENT (parent_id = NULL) của một đề thi
     * 
     * @param testId ID của đề thi
     * @return Danh sách câu hỏi parent
     */
    @Query("SELECT q FROM JlptQuestion q WHERE q.test.id = :testId AND q.parent IS NULL ORDER BY q.questionOrder ASC")
    List<JlptQuestion> findParentQuestionsByTestId(@Param("testId") Long testId);

    /**
     * Tìm tất cả câu hỏi CON của một parent
     * 
     * @param parentId ID của parent question
     * @return Danh sách câu hỏi con
     */
    @Query("SELECT q FROM JlptQuestion q WHERE q.parent.id = :parentId ORDER BY q.questionOrder ASC")
    List<JlptQuestion> findChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * Tìm câu hỏi theo section trong một đề thi
     * 
     * @param testId  ID của đề thi
     * @param section Section type (VOCABULARY, GRAMMAR, READING, LISTENING)
     * @return Danh sách câu hỏi của section đó
     */
    @Query("SELECT q FROM JlptQuestion q WHERE q.test.id = :testId AND q.section = :section ORDER BY q.questionOrder ASC")
    List<JlptQuestion> findByTestIdAndSection(@Param("testId") Long testId, @Param("section") SectionType section);

    /**
     * Tìm câu hỏi theo mondai trong một đề thi
     * 
     * @param testId       ID của đề thi
     * @param mondaiNumber Số thứ tự Mondai
     * @return Danh sách câu hỏi thuộc Mondai đó
     */
    @Query("SELECT q FROM JlptQuestion q WHERE q.test.id = :testId AND q.mondaiNumber = :mondaiNumber ORDER BY q.questionOrder ASC")
    List<JlptQuestion> findByTestIdAndMondaiNumber(@Param("testId") Long testId,
            @Param("mondaiNumber") Integer mondaiNumber);

    /**
     * Đếm số câu hỏi trong một đề thi
     * 
     * @param testId ID của đề thi
     * @return Số lượng câu hỏi
     */
    @Query("SELECT COUNT(q) FROM JlptQuestion q WHERE q.test.id = :testId")
    Long countByTestId(@Param("testId") Long testId);

    /**
     * Đếm số câu hỏi THẬT (không tính parent/passage)
     * Parent có correctOption = NULL, chỉ đếm câu có correctOption
     * 
     * @param testId ID của đề thi
     * @return Số lượng câu hỏi thật
     */
    @Query("SELECT COUNT(q) FROM JlptQuestion q WHERE q.test.id = :testId AND q.correctOption IS NOT NULL")
    Long countActualQuestionsByTestId(@Param("testId") Long testId);

    /**
     * Xóa tất cả câu hỏi của một đề thi
     * (Dùng khi cần xóa hàng loạt)
     * 
     * @param testId ID của đề thi
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM JlptQuestion q WHERE q.test.id = :testId")
    void deleteByTestId(@Param("testId") Long testId);
}
