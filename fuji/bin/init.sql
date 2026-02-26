-- ============================================================================
-- JAPANESE LEARNING PLATFORM - MySQL Database Schema (CLEAN VERSION)
-- Đã bỏ: Community, Gamification (Badge, XP, Leaderboard)
-- ============================================================================

-- ============================================================================
-- 1. USER MANAGEMENT
-- ============================================================================

CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NULL,
    google_id VARCHAR(100) UNIQUE NULL,
    
    full_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500) DEFAULT 'https://png.pngtree.com/png-vector/20190623/ourlarge/pngtree-accountavataruser--flat-color-icon--vector-icon-banner-templ-png-image_1491720.jpg',
    bio TEXT NULL,
    gender ENUM('male', 'female', 'other') DEFAULT 'other',
    phone VARCHAR(20) NULL,
    
    jlpt_level ENUM('N5', 'N4', 'N3', 'N2', 'N1') DEFAULT 'N5',
    is_active BOOLEAN DEFAULT TRUE,
    is_admin BOOLEAN DEFAULT FALSE,
    is_online BOOLEAN DEFAULT FALSE,
    
    last_active_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_sessions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    
    session_token VARCHAR(255) UNIQUE NOT NULL,
    refresh_token VARCHAR(255) UNIQUE NULL,
    
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    device_type ENUM('desktop', 'mobile', 'tablet') NULL,
    
    last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE otps (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 2. COURSE SYSTEM
-- ============================================================================

CREATE TABLE courses (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    instructor_id BIGINT UNSIGNED NOT NULL,
    thumbnail_url VARCHAR(500) DEFAULT 'https://i.postimg.cc/LXt5Hbnf/image.png',
    
    price DECIMAL(10, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'VND',
    
    student_count INT UNSIGNED DEFAULT 0,
    lesson_count INT UNSIGNED DEFAULT 0,
    total_duration INT UNSIGNED DEFAULT 0,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    rating_count INT UNSIGNED DEFAULT 0,
    
    is_published BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lessons (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(200) NOT NULL,
    lesson_order INT UNSIGNED NOT NULL,
    
    lesson_type ENUM('video', 'task') NOT NULL,
    
    video_url VARCHAR(500) NULL,
    video_type ENUM('upload', 'youtube') DEFAULT 'youtube',
    duration INT UNSIGNED DEFAULT 0,
    
    task_type ENUM('multiple_choice', 'fill_blank', 'listening', 'matching', 'speaking', 'reading') NULL,
    task_data JSON NULL,
    
    content TEXT NULL,
    
    completion_count INT UNSIGNED DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE course_enrollments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    course_id BIGINT UNSIGNED NOT NULL,
    
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_enrollment (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE course_ratings (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    course_id BIGINT UNSIGNED NOT NULL,
    rating TINYINT UNSIGNED NOT NULL CHECK (rating BETWEEN 1 AND 5),
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
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    course_id BIGINT UNSIGNED NOT NULL,
    
    current_lesson_id BIGINT UNSIGNED NULL,
    video_timestamp INT UNSIGNED DEFAULT 0,
    
    status ENUM('not_started', 'in_progress', 'completed') DEFAULT 'not_started',
    progress_percentage TINYINT UNSIGNED DEFAULT 0,
    
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    total_time_spent INT UNSIGNED DEFAULT 0,
    lessons_completed INT UNSIGNED DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (current_lesson_id) REFERENCES lessons(id) ON DELETE SET NULL,
    UNIQUE KEY unique_progress (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_lesson_completions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    lesson_id BIGINT UNSIGNED NOT NULL,
    course_id BIGINT UNSIGNED NOT NULL,
    
    score DECIMAL(5, 2) NULL,
    max_score DECIMAL(5, 2) NULL,
    attempts TINYINT UNSIGNED DEFAULT 1,
    time_spent INT UNSIGNED DEFAULT 0,
    
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_completion (user_id, lesson_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 4. JLPT TEST SYSTEM
-- ============================================================================

CREATE TABLE jlpt_tests (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    level ENUM('N5', 'N4', 'N3', 'N2', 'N1') NOT NULL,
    test_type ENUM('vocabulary', 'grammar', 'reading', 'listening', 'full_test') NOT NULL,
    
    description TEXT NULL,
    duration INT UNSIGNED NOT NULL,
    total_questions INT UNSIGNED NOT NULL,
    passing_score DECIMAL(5, 2) NOT NULL,
    
    attempt_count INT UNSIGNED DEFAULT 0,
    average_score DECIMAL(5, 2) DEFAULT 0.00,
    
    is_published BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE jlpt_questions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    test_id BIGINT UNSIGNED NOT NULL,
    question_order INT UNSIGNED NOT NULL,
    
    section ENUM('vocabulary', 'grammar', 'reading', 'listening') NOT NULL,
    question_type ENUM('multiple_choice', 'fill_blank', 'matching') NOT NULL,
    
    question_text TEXT NOT NULL,
    audio_url VARCHAR(500) NULL,
    
    options JSON NOT NULL,
    correct_answer VARCHAR(500) NOT NULL,
    explanation TEXT NULL,
    
    difficulty ENUM('easy', 'medium', 'hard') DEFAULT 'medium',
    points DECIMAL(5, 2) DEFAULT 1.00,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (test_id) REFERENCES jlpt_tests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE jlpt_test_attempts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    test_id BIGINT UNSIGNED NOT NULL,
    
    score DECIMAL(5, 2) NOT NULL,
    max_score DECIMAL(5, 2) NOT NULL,
    percentage DECIMAL(5, 2) NOT NULL,
    is_passed BOOLEAN NOT NULL,
    
    vocabulary_score DECIMAL(5, 2) DEFAULT 0,
    grammar_score DECIMAL(5, 2) DEFAULT 0,
    reading_score DECIMAL(5, 2) DEFAULT 0,
    listening_score DECIMAL(5, 2) DEFAULT 0,
    
    correct_answers INT UNSIGNED NOT NULL,
    total_questions INT UNSIGNED NOT NULL,
    time_spent INT UNSIGNED NOT NULL,
    
    answers JSON NOT NULL,
    
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (test_id) REFERENCES jlpt_tests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 5. FLASHCARD SYSTEM
-- ============================================================================

CREATE TABLE flash_lists (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    
    level ENUM('N5', 'N4', 'N3', 'N2', 'N1') DEFAULT 'N5',
    thumbnail_url VARCHAR(500) DEFAULT 'https://i.postimg.cc/LXt5Hbnf/image.png',
    
    is_public BOOLEAN DEFAULT TRUE,
    
    card_count INT UNSIGNED DEFAULT 0,
    study_count INT UNSIGNED DEFAULT 0,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    rating_count INT UNSIGNED DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE flash_cards (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    list_id BIGINT UNSIGNED NOT NULL,
    
    vocabulary VARCHAR(200) NOT NULL,
    meaning TEXT NOT NULL,
    pronunciation VARCHAR(200) NULL,
    example_sentence TEXT NULL,
    
    card_order INT UNSIGNED NOT NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (list_id) REFERENCES flash_lists(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE flash_list_ratings (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    list_id BIGINT UNSIGNED NOT NULL,
    rating TINYINT UNSIGNED NOT NULL CHECK (rating BETWEEN 1 AND 5),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (list_id) REFERENCES flash_lists(id) ON DELETE CASCADE,
    UNIQUE KEY unique_rating (user_id, list_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_card_progress (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    card_id BIGINT UNSIGNED NOT NULL,
    
    mastery_level ENUM('learning', 'reviewing', 'mastered') DEFAULT 'learning',
    correct_count INT UNSIGNED DEFAULT 0,
    incorrect_count INT UNSIGNED DEFAULT 0,
    
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
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(200) NULL,
    
    conversation_type ENUM('general', 'learning_support', 'jlpt_prep', 'grammar_help') DEFAULT 'general',
    context_data JSON NULL,
    
    message_count INT UNSIGNED DEFAULT 0,
    is_archived BOOLEAN DEFAULT FALSE,
    
    last_message_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ai_messages (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT UNSIGNED NOT NULL,
    
    role ENUM('user', 'assistant') NOT NULL,
    content TEXT NOT NULL,
    
    tokens_used INT UNSIGNED NULL,
    model_version VARCHAR(50) NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 7. PAYMENT SYSTEM
-- ============================================================================

CREATE TABLE payment_methods (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    
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
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    
    transaction_type ENUM('course_purchase', 'subscription', 'refund') NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'VND',
    
    payment_method_id BIGINT UNSIGNED NULL,
    payment_gateway VARCHAR(50) NULL,
    gateway_transaction_id VARCHAR(200) NULL,
    
    status ENUM('pending', 'processing', 'completed', 'failed', 'refunded') DEFAULT 'pending',
    
    related_entity_type ENUM('course', 'subscription') NULL,
    related_entity_id BIGINT UNSIGNED NULL,
    
    description TEXT NULL,
    metadata JSON NULL,
    
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE transaction_logs (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT UNSIGNED NOT NULL,
    
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
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    date DATE NOT NULL,
    
    total_study_time INT UNSIGNED DEFAULT 0,
    lessons_completed TINYINT UNSIGNED DEFAULT 0,
    
    cards_reviewed INT UNSIGNED DEFAULT 0,
    cards_learned INT UNSIGNED DEFAULT 0,
    
    correct_rate DECIMAL(5, 2) DEFAULT 0,
    
    streak_days INT UNSIGNED DEFAULT 0, -- Giữ lại để biết chăm học, ko thưởng badge
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_insights (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    
    analysis_date DATE NOT NULL,
    
    overall_level ENUM('beginner', 'intermediate', 'advanced') NULL,
    weekly_progress DECIMAL(5, 2) DEFAULT 0,
    consistency_score TINYINT UNSIGNED DEFAULT 0,
    retention_rate DECIMAL(5, 2) DEFAULT 0,
    
    listening_level TINYINT UNSIGNED DEFAULT 0,
    speaking_level TINYINT UNSIGNED DEFAULT 0,
    reading_level TINYINT UNSIGNED DEFAULT 0,
    writing_level TINYINT UNSIGNED DEFAULT 0,
    
    best_study_time ENUM('morning', 'afternoon', 'evening', 'night') NULL,
    avg_session_length INT UNSIGNED DEFAULT 0,
    study_frequency DECIMAL(3, 1) DEFAULT 0,
    
    ai_message TEXT NULL,
    ai_tone ENUM('encouraging', 'motivating', 'supportive', 'constructive') DEFAULT 'encouraging',
    ai_generated_at TIMESTAMP NULL,
    
    model_version VARCHAR(50) NULL,
    confidence_score TINYINT UNSIGNED DEFAULT 0,
    data_points_analyzed INT UNSIGNED DEFAULT 0,
    
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_analysis (user_id, analysis_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE lesson_recommendations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    lesson_id BIGINT UNSIGNED NOT NULL,
    course_id BIGINT UNSIGNED NOT NULL,
    
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
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT NOT NULL,
    setting_type ENUM('string', 'number', 'boolean', 'json') DEFAULT 'string',
    description TEXT NULL,
    
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_preferences (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    
    daily_goal_minutes INT UNSIGNED DEFAULT 30,
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
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    
    type ENUM('course', 'system', 'reminder') NOT NULL, -- Đã xóa 'achievement'
    title VARCHAR(200) NOT NULL,
    content TEXT NULL,
    
    link_url VARCHAR(500) NULL,
    
    related_type VARCHAR(50) NULL,
    related_id BIGINT UNSIGNED NULL,
    
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NULL,
    
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NULL,
    entity_id BIGINT UNSIGNED NULL,
    
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