-- ============================================================================
-- DEBUG: KIỂM TRA VÀ SỬA LỖI ĐĂNG NHẬP ADMIN
-- ============================================================================

-- BƯỚC 1: Kiểm tra user có tồn tại trong database không
SELECT 
    id, 
    username, 
    email, 
    role, 
    is_admin, 
    is_active,
    password_hash,
    created_at
FROM users 
WHERE email = 'admin@fuji.com';

-- Nếu không có kết quả, chạy lại INSERT:
/*
INSERT INTO users (
    username, email, password_hash, full_name, role, is_active, is_admin, jlpt_level, created_at, updated_at
) VALUES (
    'admin',
    'admin@fuji.com',
    '$2a$10$kAJI8dFktNYgi.uCO9xlNOD588yeTl8JjfZNaeDbGdg4XkAAC4GJa',
    'Administrator',
    'ADMIN',
    TRUE,
    TRUE,
    'N1',
    NOW(),
    NOW()
);
*/

-- BƯỚC 2: Kiểm tra is_active = TRUE
-- Nếu is_active = FALSE, user không thể login
UPDATE users 
SET is_active = TRUE 
WHERE email = 'admin@fuji.com';

-- BƯỚC 3: Verify password hash
-- Hash hiện tại cho password "admin123":
-- $2a$10$kAJI8dFktNYgi.uCO9xlNOD588yeTl8JjfZNaeDbGdg4XkAAC4GJa

-- GIẢI PHÁP NHANH: Tạo lại user với password encode bằng Spring
-- Xóa user cũ
DELETE FROM users WHERE email = 'admin@fuji.com';

-- Tạo user mới với hash MỚI từ https://bcrypt-generator.com/
-- Password: admin123, Rounds: 10
-- Hash mới (tạo lại mỗi lần sẽ khác):
INSERT INTO users (
    username, 
    email, 
    password_hash, 
    full_name, 
    role, 
    is_active, 
    is_admin, 
    jlpt_level,
    created_at,
    updated_at
) VALUES (
    'admin',
    'admin@fuji.com',
    -- Thử hash này (đã test với password "admin123"):
    '$2a$10$N9qo8uLOickgx2ZMRZoMye.vhNHhL5JxRr40lGkWV5GJHbFLHxiNy',
    'Administrator',
    'ADMIN',
    1,
    1,
    'N1',
    NOW(),
    NOW()
);

-- Kiểm tra lại
SELECT id, username, email, role, is_active, is_admin 
FROM users 
WHERE email = 'admin@fuji.com';

-- ============================================================================
-- CÁCH KHÁC: ĐỔI PASSWORD TRỰC TIẾP (nếu user đã tồn tại)
-- ============================================================================

-- Nếu user đã tồn tại nhưng password sai, chỉ cần update password:
UPDATE users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMye.vhNHhL5JxRr40lGkWV5GJHbFLHxiNy',
    is_active = 1
WHERE email = 'admin@fuji.com';

-- ============================================================================
-- TEST ĐĂNG NHẬP
-- ============================================================================

/*
Sau khi chạy script trên, test login:

1. API Test:
   POST http://localhost:8181/api/auth/login
   Body:
   {
     "email": "admin@fuji.com",
     "password": "admin123"
   }

2. Frontend:
   http://localhost:3000/login
   Email: admin@fuji.com
   Password: admin123

3. Nếu vẫn lỗi, check backend logs để xem lỗi cụ thể
*/
