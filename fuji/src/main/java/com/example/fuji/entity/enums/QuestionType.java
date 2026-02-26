package com.example.fuji.entity.enums;

/**
 * Enum định nghĩa các loại câu hỏi trong bài thi JLPT
 * 
 * MULTIPLE_CHOICE: Câu hỏi trắc nghiệm nhiều lựa chọn (Ví dụ: Chọn đáp án đúng
 * A, B, C, D)
 * FILL_BLANK: Câu hỏi điền vào chỗ trống
 * MATCHING: Câu hỏi ghép đôi (Ví dụ: Ghép từ với nghĩa)
 */
public enum QuestionType {
    MULTIPLE_CHOICE, // Trắc nghiệm
    FILL_BLANK, // Điền vào chỗ trống
    MATCHING // Ghép đôi
}
