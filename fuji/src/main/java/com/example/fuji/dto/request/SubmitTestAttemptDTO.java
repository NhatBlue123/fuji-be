package com.example.fuji.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để submit bài thi JLPT
 * 
 * User gửi lên danh sách câu trả lời
 * Backend sẽ:
 * 1. Tính điểm tổng và điểm từng phần
 * 2. Kiểm tra pass/fail (dựa vào điểm liệt)
 * 3. Lưu vào jlpt_test_attempts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTestAttemptDTO {

    @NotNull(message = "ID đề thi không được để trống")
    private Long testId;

    /**
     * Thời gian làm bài (giây)
     */
    @NotNull(message = "Thời gian làm bài không được để trống")
    private Integer timeSpent;

    /**
     * Danh sách câu trả lời (JSON string)
     * 
     * Format:
     * [
     * { "question_id": 101, "selected": 2 },
     * { "question_id": 102, "selected": 4 },
     * { "question_id": 103, "selected": 1 }
     * ]
     * 
     * Backend sẽ so sánh với correct_option và tính điểm
     */
    @NotNull(message = "Danh sách câu trả lời không được để trống")
    private String userAnswers;
}
