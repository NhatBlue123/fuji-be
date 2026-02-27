package com.example.fuji.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.JlptTestAttempt;
import com.example.fuji.entity.enums.AttemptStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository để truy vấn bảng jlpt_test_attempts
 */
@Repository
public interface JlptTestAttemptRepository extends JpaRepository<JlptTestAttempt, Long> {

        /**
         * Tìm tất cả lượt thi của một user, sắp xếp theo thời gian mới nhất
         * 
         * @param userId ID của user
         * @return Danh sách kết quả thi
         */
        @Query("SELECT a FROM JlptTestAttempt a WHERE a.user.id = :userId ORDER BY a.startedAt DESC")
        List<JlptTestAttempt> findByUserIdOrderByStartedAtDesc(@Param("userId") Long userId);

        /**
         * Tìm tất cả lượt thi của một user với phân trang
         * 
         * @param userId   ID của user
         * @param pageable Phân trang
         * @return Page chứa kết quả thi
         */
        @Query("SELECT a FROM JlptTestAttempt a WHERE a.user.id = :userId ORDER BY a.startedAt DESC")
        Page<JlptTestAttempt> findByUserId(@Param("userId") Long userId, Pageable pageable);

        /**
         * Tìm tất cả lượt thi của một đề thi cụ thể
         * 
         * @param testId ID của đề thi
         * @return Danh sách kết quả thi
         */
        @Query("SELECT a FROM JlptTestAttempt a WHERE a.test.id = :testId ORDER BY a.startedAt DESC")
        List<JlptTestAttempt> findByTestIdOrderByStartedAtDesc(@Param("testId") Long testId);

        /**
         * Tìm tất cả lượt thi của một user với một đề thi cụ thể
         * 
         * @param userId ID của user
         * @param testId ID của đề thi
         * @return Danh sách kết quả thi
         */
        @Query("SELECT a FROM JlptTestAttempt a WHERE a.user.id = :userId AND a.test.id = :testId ORDER BY a.startedAt DESC")
        List<JlptTestAttempt> findByUserIdAndTestId(@Param("userId") Long userId, @Param("testId") Long testId);

        /**
         * Tìm tất cả lượt thi ĐỖ của một user
         * 
         * @param userId ID của user
         * @return Danh sách kết quả thi đỗ
         */
        @Query("SELECT a FROM JlptTestAttempt a WHERE a.user.id = :userId AND a.isPassed = true ORDER BY a.startedAt DESC")
        List<JlptTestAttempt> findPassedAttemptsByUserId(@Param("userId") Long userId);

        /**
         * Đếm số lần đã thi của một user với một đề thi
         * 
         * @param userId ID của user
         * @param testId ID của đề thi
         * @return Số lần đã thi
         */
        @Query("SELECT COUNT(a) FROM JlptTestAttempt a WHERE a.user.id = :userId AND a.test.id = :testId")
        Long countByUserIdAndTestId(@Param("userId") Long userId, @Param("testId") Long testId);

        // ========================================================================
        // ANTI-CHEAT REPOSITORY METHODS
        // ========================================================================

        /**
         * Tìm bài thi đang trong trạng thái IN_PROGRESS của một user cho một đề cụ thể
         */
        @Query("SELECT a FROM JlptTestAttempt a WHERE a.user.id = :userId AND a.test.id = :testId AND a.status = :status")
        List<JlptTestAttempt> findByUserIdAndTestIdAndStatus(@Param("userId") Long userId, @Param("testId") Long testId,
                        @Param("status") AttemptStatus status);

        /**
         * Lấy danh sách các bài thi IN_PROGRESS đã vượt quá expiresAt
         */
        @Query("SELECT a FROM JlptTestAttempt a WHERE a.status = :status AND a.expiresAt < :now")
        List<JlptTestAttempt> findExpiredInProgressAttempts(@Param("status") AttemptStatus status,
                        @Param("now") LocalDateTime now);

        /**
         * Lấy bài thi và khóa Row (Pessimistic Lock) để chống double-submit
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT a FROM JlptTestAttempt a WHERE a.id = :id")
        Optional<JlptTestAttempt> findByIdWithLock(@Param("id") Long id);
}
