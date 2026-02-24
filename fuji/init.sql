-- ============================================================================
-- JAPANESE LEARNING PLATFORM - MySQL Database Schema (CLEAN VERSION)
-- Đã bỏ: Community, Gamification (Badge, XP, Leaderboard)
-- ============================================================================

-- ============================================================================
-- 1. USER MANAGEMENT
-- ============================================================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NULL,
    google_id VARCHAR(100) UNIQUE NULL,

    full_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500) DEFAULT 'https://png.pngtree.com/png-vector/20190623/ourlarge/pngtree-accountavataruser--flat-color-icon--vector-icon-banner-templ-png-image_1491720.jpg',
    bio TEXT NULL,
    gender ENUM('male', 'female', 'other') DEFAULT 'other',
    phone VARCHAR(20) NULL,

    role ENUM('STUDENT', 'INSTRUCTOR', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    jlpt_level ENUM('N5', 'N4', 'N3', 'N2', 'N1') DEFAULT 'N5',
    is_active BOOLEAN DEFAULT FALSE,
    is_admin BOOLEAN DEFAULT FALSE,
    is_online BOOLEAN DEFAULT FALSE,

    last_active_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    token VARCHAR(255) UNIQUE NOT NULL,

    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE otps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,

    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- 2. COURSE SYSTEM
-- ============================================================================

CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    instructor_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    thumbnail_url VARCHAR(500) DEFAULT 'https://i.postimg.cc/LXt5Hbnf/image.png',

    price DECIMAL(10, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'VND',

    student_count INT DEFAULT 0,
    lesson_count INT DEFAULT 0,
    total_duration INT DEFAULT 0,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    rating_count INT DEFAULT 0,

    is_published BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_instructor_id (instructor_id),
    INDEX idx_created_by (created_by),
    INDEX idx_is_published (is_published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lessons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    lesson_order INT NOT NULL,

    lesson_type ENUM('video', 'task') NOT NULL,

    video_url VARCHAR(500) NULL,
    video_type ENUM('upload', 'youtube') DEFAULT 'youtube',
    duration INT DEFAULT 0,

    task_type ENUM('multiple_choice', 'fill_blank', 'listening', 'matching', 'speaking', 'reading') NULL,
    task_data JSON NULL,

    content TEXT NULL,

    completion_count INT DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE course_enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,

    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_enrollment (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE course_ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review TEXT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_rating (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 3. USER PROGRESS TRACKING
-- ============================================================================

CREATE TABLE user_course_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,

    current_lesson_id BIGINT NULL,
    video_timestamp INT DEFAULT 0,

    status ENUM('not_started', 'in_progress', 'completed') DEFAULT 'not_started',
    progress_percentage TINYINT DEFAULT 0,

    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    total_time_spent INT DEFAULT 0,
    lessons_completed INT DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (current_lesson_id) REFERENCES lessons(id) ON DELETE SET NULL,
    UNIQUE KEY unique_progress (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_lesson_completions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,

    score DECIMAL(5, 2) NULL,
    max_score DECIMAL(5, 2) NULL,
    attempts TINYINT DEFAULT 1,
    time_spent INT DEFAULT 0,

    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_completion (user_id, lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 4. JLPT TEST SYSTEM (Redesigned - Real JLPT Structure)
-- ============================================================================

-- ==========================================
-- 4.1. BẢNG QUẢN LÝ MEDIA (Cloudinary)
-- ==========================================
CREATE TABLE media_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Cloudinary metadata (QUAN TRỌNG để xóa file)
    cloudinary_public_id VARCHAR(255) NOT NULL UNIQUE COMMENT 'Public ID để xóa file trên Cloudinary',
    cloudinary_url VARCHAR(500) NOT NULL COMMENT 'URL đầy đủ của file',
    
    -- Thông tin file
    resource_type ENUM('image', 'audio', 'video', 'raw') NOT NULL COMMENT 'Loại file',
    format VARCHAR(10) NULL COMMENT 'jpg, png, mp3, wav...',
    file_size BIGINT NULL COMMENT 'Kích thước file (bytes)',
    
    -- Metadata cho image/video
    width INT NULL COMMENT 'Chiều rộng (px)',
    height INT NULL COMMENT 'Chiều cao (px)',
    
    -- Metadata cho audio/video
    duration DECIMAL(10, 2) NULL COMMENT 'Thời lượng (giây)',
    
    -- Thông tin bổ sung
    original_filename VARCHAR(255) NULL COMMENT 'Tên file gốc khi user upload',
    uploaded_by BIGINT NULL COMMENT 'User đã upload',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_public_id (cloudinary_public_id),
    INDEX idx_resource_type (resource_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 4.2. BẢNG ĐỀ THI (Thêm cấu hình điểm liệt)
-- ==========================================
CREATE TABLE jlpt_tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT 'VD: JLPT N2 Tháng 7/2023',
    level ENUM('N5', 'N4', 'N3', 'N2', 'N1') NOT NULL,
    
    -- Loại đề: Full test (thi thử thật) hoặc Luyện tập từng phần
    test_type ENUM('full_test', 'vocabulary', 'grammar', 'reading', 'listening') NOT NULL,

    description TEXT NULL,
    duration INT NOT NULL COMMENT 'Tổng thời gian (phút)',
    total_questions INT NOT NULL,
    
    -- --- CẤU HÌNH ĐIỂM SỐ (QUAN TRỌNG) ---
    max_score INT DEFAULT 180 COMMENT 'Tổng điểm tối đa (chuẩn JLPT là 180)',
    pass_score INT NOT NULL COMMENT 'Điểm đỗ tổng (VD: N2 là 90, N1 là 100)',
    
    -- Điểm liệt (Nếu thấp hơn mức này là trượt, bất kể tổng điểm)
    -- Với N1-N3: Chia làm: Kiến thức ngôn ngữ / Đọc / Nghe
    -- Với N4-N5: Chia làm: Kiến thức ngôn ngữ (Từ vựng/Ngữ pháp/Đọc) / Nghe
    language_knowledge_pass_score INT DEFAULT 19 COMMENT 'Điểm liệt phần kiến thức ngôn ngữ', 
    reading_pass_score INT DEFAULT 19 COMMENT 'Điểm liệt phần đọc',
    listening_pass_score INT DEFAULT 19 COMMENT 'Điểm liệt phần nghe',

    -- Thống kê
    attempt_count INT DEFAULT 0,
    average_score DECIMAL(5, 2) DEFAULT 0.00,
    is_published BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_level (level),
    INDEX idx_test_type (test_type),
    INDEX idx_is_published (is_published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 4.3. BẢNG CÂU HỎI (Hỗ trợ Mondai & Parent-Child)
-- ==========================================
CREATE TABLE jlpt_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_id BIGINT NOT NULL,
    
    -- XỬ LÝ MONDAI (Bài lớn) - VD: Mondai 1, Mondai 2...
    mondai_number INT NOT NULL COMMENT 'Số thứ tự Mondai (VD: 1, 2, 3)', 
    mondai_title VARCHAR(255) NULL COMMENT 'Tiêu đề Mondai (VD: Problem 1: Kanji Reading)', 
    
    -- XỬ LÝ CẤU TRÚC CHA-CON (QUAN TRỌNG NHẤT)
    -- Nếu dòng này là đoạn văn bài đọc -> parent_id = NULL
    -- Nếu dòng này là câu hỏi nhỏ thuộc bài đọc đó -> parent_id = ID của đoạn văn
    parent_id BIGINT NULL COMMENT 'ID câu hỏi cha (NULL nếu là parent)',
    
    question_order INT NOT NULL COMMENT 'Thứ tự hiển thị trong đề',

    section ENUM('vocabulary', 'grammar', 'reading', 'listening') NOT NULL,
    
    -- NỘI DUNG (Context-Aware)
    -- Nếu là parent: Chứa đoạn văn/hội thoại
    -- Nếu là child/câu đơn: Chứa câu hỏi
    content_text TEXT NOT NULL COMMENT 'Nội dung câu hỏi hoặc đoạn văn', 
    
    -- MEDIA (Hỗ trợ hình ảnh/âm thanh cho cả câu cha và câu con)
    -- Liên kết với bảng media_files thay vì lưu URL trực tiếp
    image_media_id BIGINT NULL COMMENT 'ID của file ảnh trong media_files',
    audio_media_id BIGINT NULL COMMENT 'ID của file audio trong media_files',

    -- ĐÁP ÁN (Chỉ câu hỏi mới có, đoạn văn cha thì để NULL)
    options JSON NULL COMMENT 'Mảng JSON: ["1. ...", "2. ...", "3. ...", "4. ..."]', 
    correct_option INT NULL COMMENT 'Đáp án đúng (index: 1, 2, 3, 4)', 
    
    explanation TEXT NULL COMMENT 'Giải thích chi tiết đáp án',
    points DECIMAL(5, 2) DEFAULT 1.00 COMMENT 'Điểm số câu này',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (test_id) REFERENCES jlpt_tests(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES jlpt_questions(id) ON DELETE CASCADE,
    FOREIGN KEY (image_media_id) REFERENCES media_files(id) ON DELETE SET NULL,
    FOREIGN KEY (audio_media_id) REFERENCES media_files(id) ON DELETE SET NULL,
    
    INDEX idx_test_id (test_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_mondai (mondai_number),
    INDEX idx_section (section)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- 4.4. BẢNG KẾT QUẢ THI (Chi tiết hoá)
-- ==========================================
CREATE TABLE jlpt_test_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    test_id BIGINT NOT NULL,

    -- Kết quả tổng quan
    total_score DECIMAL(5, 2) NOT NULL COMMENT 'Tổng điểm đạt được',
    is_passed BOOLEAN NOT NULL COMMENT 'Pass = Tổng >= pass_score VÀ các phần >= điểm liệt',

    -- Lưu điểm thành phần để hiển thị biểu đồ
    language_knowledge_score DECIMAL(5, 2) DEFAULT 0 COMMENT 'Điểm phần kiến thức ngôn ngữ',
    reading_score DECIMAL(5, 2) DEFAULT 0 COMMENT 'Điểm phần đọc',
    listening_score DECIMAL(5, 2) DEFAULT 0 COMMENT 'Điểm phần nghe',

    -- Thống kê
    correct_answers INT NOT NULL,
    total_questions INT NOT NULL,
    time_spent INT NOT NULL COMMENT 'Thời gian làm bài (giây)',

    -- Lưu chi tiết bài làm (SNAPSHOT)
    -- JSON structure:
    -- [
    --   { "question_id": 101, "selected": 2, "correct": 2, "is_correct": true },
    --   { "question_id": 102, "selected": 1, "correct": 4, "is_correct": false }
    -- ]
    user_answers JSON NOT NULL, 
    
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (test_id) REFERENCES jlpt_tests(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_test_id (test_id),
    INDEX idx_is_passed (is_passed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 5. FLASHCARD SYSTEM
-- ============================================================================

CREATE TABLE flash_lists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,

    level ENUM('N5', 'N4', 'N3', 'N2', 'N1') DEFAULT 'N5',
    thumbnail_url VARCHAR(500) DEFAULT 'https://i.postimg.cc/LXt5Hbnf/image.png',

    is_public BOOLEAN DEFAULT TRUE,

    card_count INT DEFAULT 0,
    study_count INT DEFAULT 0,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    rating_count INT DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE flash_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    list_id BIGINT NOT NULL,

    vocabulary VARCHAR(200) NOT NULL,
    meaning TEXT NOT NULL,
    pronunciation VARCHAR(200) NULL,
    example_sentence TEXT NULL,

    card_order INT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (list_id) REFERENCES flash_lists(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE flash_list_ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    list_id BIGINT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (list_id) REFERENCES flash_lists(id) ON DELETE CASCADE,
    UNIQUE KEY unique_rating (user_id, list_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_card_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL,

    mastery_level ENUM('learning', 'reviewing', 'mastered') DEFAULT 'learning',
    correct_count INT DEFAULT 0,
    incorrect_count INT DEFAULT 0,

    last_reviewed_at TIMESTAMP NULL,
    next_review_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES flash_cards(id) ON DELETE CASCADE,
    UNIQUE KEY unique_progress (user_id, card_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 6. AI CONVERSATION SYSTEM
-- ============================================================================

CREATE TABLE ai_conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NULL,

    conversation_type ENUM('general', 'learning_support', 'jlpt_prep', 'grammar_help') DEFAULT 'general',
    context_data JSON NULL,

    message_count INT DEFAULT 0,
    is_archived BOOLEAN DEFAULT FALSE,

    last_message_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,

    role ENUM('user', 'assistant') NOT NULL,
    content TEXT NOT NULL,

    tokens_used INT NULL,
    model_version VARCHAR(50) NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 7. PAYMENT SYSTEM
-- ============================================================================

CREATE TABLE payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    method_type ENUM('credit_card', 'debit_card', 'bank_transfer', 'e_wallet', 'momo', 'zalopay') NOT NULL,

    card_last_four VARCHAR(4) NULL,
    card_brand VARCHAR(50) NULL,

    is_default BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    transaction_type ENUM('course_purchase', 'subscription', 'refund') NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'VND',

    payment_method_id BIGINT NULL,
    payment_gateway VARCHAR(50) NULL,
    gateway_transaction_id VARCHAR(200) NULL,

    status ENUM('pending', 'processing', 'completed', 'failed', 'refunded') DEFAULT 'pending',

    related_entity_type ENUM('course', 'subscription') NULL,
    related_entity_id BIGINT NULL,

    description TEXT NULL,
    metadata JSON NULL,

    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transaction_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,

    status ENUM('pending', 'processing', 'completed', 'failed', 'refunded') NOT NULL,
    message TEXT NULL,
    metadata JSON NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 8. LEARNING ANALYTICS (Giữ lại để làm báo cáo, ko phải Gamification)
-- ============================================================================

CREATE TABLE daily_learning_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,

    total_study_time INT DEFAULT 0,
    lessons_completed TINYINT DEFAULT 0,

    cards_reviewed INT DEFAULT 0,
    cards_learned INT DEFAULT 0,

    correct_rate DECIMAL(5, 2) DEFAULT 0,

    streak_days INT DEFAULT 0, -- Giữ lại để biết chăm học, ko thưởng badge

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_insights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    analysis_date DATE NOT NULL,

    overall_level ENUM('beginner', 'intermediate', 'advanced') NULL,
    weekly_progress DECIMAL(5, 2) DEFAULT 0,
    consistency_score TINYINT DEFAULT 0,
    retention_rate DECIMAL(5, 2) DEFAULT 0,

    listening_level TINYINT DEFAULT 0,
    speaking_level TINYINT DEFAULT 0,
    reading_level TINYINT DEFAULT 0,
    writing_level TINYINT DEFAULT 0,

    best_study_time ENUM('morning', 'afternoon', 'evening', 'night') NULL,
    avg_session_length INT DEFAULT 0,
    study_frequency DECIMAL(3, 1) DEFAULT 0,

    ai_message TEXT NULL,
    ai_tone ENUM('encouraging', 'motivating', 'supportive', 'constructive') DEFAULT 'encouraging',
    ai_generated_at TIMESTAMP NULL,

    model_version VARCHAR(50) NULL,
    confidence_score TINYINT DEFAULT 0,
    data_points_analyzed INT DEFAULT 0,

    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_analysis (user_id, analysis_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lesson_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,

    priority ENUM('high', 'medium', 'low') DEFAULT 'medium',
    reason ENUM('continue_course', 'improve_weak_skill', 'review', 'recommended') NOT NULL,
    score DECIMAL(5, 2) DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 9. SYSTEM SETTINGS
-- ============================================================================

CREATE TABLE system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT NOT NULL,
    setting_type ENUM('string', 'number', 'boolean', 'json') DEFAULT 'string',
    description TEXT NULL,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    daily_goal_minutes INT DEFAULT 30,
    reminder_enabled BOOLEAN DEFAULT FALSE,
    reminder_time TIME NULL,

    email_notifications BOOLEAN DEFAULT TRUE,
    push_notifications BOOLEAN DEFAULT TRUE,
    marketing_emails BOOLEAN DEFAULT FALSE,

    show_activity BOOLEAN DEFAULT TRUE,
    show_statistics BOOLEAN DEFAULT TRUE,

    language VARCHAR(10) DEFAULT 'vi',
    theme ENUM('light', 'dark', 'auto') DEFAULT 'light',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_pref (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 10. SUPPORTING TABLES (Đã bỏ Achievement Notification)
-- ============================================================================

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,

    type ENUM('course', 'system', 'reminder') NOT NULL, -- Đã xóa 'achievement'
    title VARCHAR(200) NOT NULL,
    content TEXT NULL,

    link_url VARCHAR(500) NULL,

    related_type VARCHAR(50) NULL,
    related_id BIGINT NULL,

    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,

    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NULL,
    entity_id BIGINT NULL,

    old_values JSON NULL,
    new_values JSON NULL,

    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- INITIAL DATA SEEDING (Đã bỏ Achievement Seeding)
-- ============================================================================

INSERT INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
('site_name', 'Japanese Learning Platform', 'string', 'Website name'),
('max_upload_size', '10485760', 'number', 'Max file upload size in bytes (10MB)'),
('maintenance_mode', 'false', 'boolean', 'Enable maintenance mode'),
('free_trial_days', '7', 'number', 'Free trial period in days'),
('jlpt_pass_percentage', '70', 'number', 'Minimum percentage to pass JLPT test');

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================