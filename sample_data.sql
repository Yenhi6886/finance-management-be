-- Dữ liệu mẫu cho hệ thống quản lý tài chính
-- Tạo bởi GitHub Copilot

-- ===============================================
-- DỮ LIỆU BẢNG USERS
-- ===============================================

-- Xóa dữ liệu cũ (nếu có)
DELETE FROM user_settings;
DELETE FROM wallets;
DELETE FROM users;

-- Reset AUTO_INCREMENT
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE wallets AUTO_INCREMENT = 1;

-- Thêm dữ liệu users
INSERT INTO users (email, username, password, first_name, last_name, phone_number, avatar_url, status, auth_provider, provider_id, created_at, updated_at) VALUES
-- User đăng ký local
('admin@gmail.com', 'admin', '$2a$10$9Rgbdp5AHUC4zGQCvJYULOJZCDZR7JZkPYi6LSF8mAl5HqGi9uSEW', 'Nguyễn', 'Quản Trị', '0987654321', 'avatars/admin-avatar.jpg', 'ACTIVE', 'LOCAL', NULL, NOW(), NOW()),
('john.doe@gmail.com', 'johndoe', '$2a$10$9Rgbdp5AHUC4zGQCvJYULOJZCDZR7JZkPYi6LSF8mAl5HqGi9uSEW', 'John', 'Doe', '0901234567', 'avatars/john-avatar.jpg', 'ACTIVE', 'LOCAL', NULL, NOW(), NOW()),
('jane.smith@gmail.com', 'janesmith', '$2a$10$9Rgbdp5AHUC4zGQCvJYULOJZCDZR7JZkPYi6LSF8mAl5HqGi9uSEW', 'Jane', 'Smith', '0912345678', 'avatars/jane-avatar.jpg', 'ACTIVE', 'LOCAL', NULL, NOW(), NOW()),

-- User đăng nhập Google OAuth2
('google.user1@gmail.com', 'googleuser1', '$2a$10$9Rgbdp5AHUC4zGQCvJYULOJZCDZR7JZkPYi6LSF8mAl5HqGi9uSEW', 'Trần', 'Văn Minh', '0923456789', 'https://lh3.googleusercontent.com/a/sample1', 'ACTIVE', 'GOOGLE', '115123456789012345678', NOW(), NOW()),
('google.user2@gmail.com', 'googleuser2', '$2a$10$9Rgbdp5AHUC4zGQCvJYULOJZCDZR7JZkPYi6LSF8mAl5HqGi9uSEW', 'Lê', 'Thị Hoa', '0934567890', 'https://lh3.googleusercontent.com/a/sample2', 'ACTIVE', 'GOOGLE', '115987654321098765432', NOW(), NOW()),

-- User chưa kích hoạt
('inactive.user@gmail.com', 'inactiveuser', '$2a$10$9Rgbdp5AHUC4zGQCvJYULOJZCDZR7JZkPYi6LSF8mAl5HqGi9uSEW', 'Phạm', 'Văn Nam', '0945678901', NULL, 'INACTIVE', 'LOCAL', NULL, NOW(), NOW()),

-- User bị khóa
('locked.user@gmail.com', 'lockeduser', '$2a$10$9Rgbdp5AHUC4zGQCvJYULOJZCDZR7JZkPYi6LSF8mAl5HqGi9uSEW', 'Vũ', 'Thị Lan', '0956789012', NULL, 'LOCKED', 'LOCAL', NULL, NOW(), NOW()),

-- User từ Facebook
('facebook.user@gmail.com', 'facebookuser', '$2a$10$9Rgbdp5AHUC4zGQCvJYULOJZCDZR7JZkPYi6LSF8mAl5HqGi9uSEW', 'Hoàng', 'Văn Đức', '0967890123', 'https://graph.facebook.com/123456789/picture', 'ACTIVE', 'FACEBOOK', 'fb_123456789012345', NOW(), NOW()),

-- User từ GitHub
('github.user@gmail.com', 'githubuser', '$2a$10$9Rgbdp5AHUC4zGQCvJYULOJZCDZR7JZkPYi6LSF8mAl5HqGi9uSEW', 'Đặng', 'Thị Mai', '0978901234', 'https://avatars.githubusercontent.com/u/12345678', 'ACTIVE', 'GITHUB', 'gh_12345678', NOW(), NOW()),

-- User mới đăng ký cần kích hoạt
('newuser@gmail.com', 'newuser', '$2a$10$9Rgbdp5AHUC4zGQCvJYULOJZCDZR7JZkPYi6LSF8mAl5HqGi9uSEW', 'Bùi', 'Văn Tùng', '0989012345', NULL, 'INACTIVE', 'LOCAL', NULL, NOW(), NOW());

-- ===============================================
-- DỮ LIỆU BẢNG WALLETS
-- ===============================================

INSERT INTO wallets (user_id, name, icon, balance, currency_code, description, is_archived, created_at, updated_at) VALUES
-- Ví cho Admin (user_id = 1)
(1, 'Ví tiền mặt', '💵', 5000000.00, 'VND', 'Ví chứa tiền mặt chính', false, NOW(), NOW()),
(1, 'Tài khoản ngân hàng', '🏦', 50000000.00, 'VND', 'Tài khoản tiết kiệm Vietcombank', false, NOW(), NOW()),
(1, 'Ví đầu tư', '📈', 25000000.00, 'VND', 'Ví dành cho đầu tư chứng khoán', false, NOW(), NOW()),

-- Ví cho John Doe (user_id = 2)
(2, 'Cash Wallet', '💰', 1000000.00, 'VND', 'Main cash wallet', false, NOW(), NOW()),
(2, 'Savings Account', '💳', 15000000.00, 'VND', 'BIDV savings account', false, NOW(), NOW()),
(2, 'Emergency Fund', '🚨', 10000000.00, 'VND', 'Emergency savings', false, NOW(), NOW()),

-- Ví cho Jane Smith (user_id = 3)
(3, 'Ví chính', '👜', 2000000.00, 'VND', 'Ví sử dụng hàng ngày', false, NOW(), NOW()),
(3, 'Thẻ tín dụng', '💳', -500000.00, 'VND', 'Thẻ tín dụng Techcombank', false, NOW(), NOW()),
(3, 'Ví đi chợ', '🛒', 500000.00, 'VND', 'Tiền mua sắm hàng tuần', false, NOW(), NOW()),

-- Ví cho Google User 1 (user_id = 4)
(4, 'Ví Google Pay', '📱', 3000000.00, 'VND', 'Ví điện tử Google Pay', false, NOW(), NOW()),
(4, 'Tài khoản chính', '🏛️', 20000000.00, 'VND', 'Tài khoản MB Bank', false, NOW(), NOW()),

-- Ví cho Google User 2 (user_id = 5)
(5, 'Ví sinh viên', '🎓', 1500000.00, 'VND', 'Ví dành cho sinh viên', false, NOW(), NOW()),
(5, 'Ví part-time', '⏰', 2500000.00, 'VND', 'Tiền từ công việc bán thời gian', false, NOW(), NOW()),

-- Ví cho Facebook User (user_id = 8)
(8, 'Ví Facebook', '📘', 800000.00, 'VND', 'Ví liên kết Facebook', false, NOW(), NOW()),
(8, 'Ví kinh doanh', '💼', 5000000.00, 'VND', 'Ví dành cho kinh doanh online', false, NOW(), NOW()),

-- Ví cho GitHub User (user_id = 9)
(9, 'Developer Wallet', '💻', 12000000.00, 'VND', 'Ví thu nhập từ lập trình', false, NOW(), NOW()),
(9, 'Crypto Wallet', '₿', 8000000.00, 'VND', 'Ví đầu tư tiền điện tử', false, NOW(), NOW());

-- ===============================================
-- DỮ LIỆU BẢNG USER_SETTINGS
-- ===============================================

INSERT INTO user_settings (user_id, language, currency_format, date_format, daily_report, weekly_report, monthly_report, current_wallet_id, updated_at) VALUES
-- Cài đặt cho Admin (user_id = 1)
(1, 'vi', 'dot_separator', 'DD_MM_YYYY', true, true, true, 1, NOW()),

-- Cài đặt cho John Doe (user_id = 2)
(2, 'en', 'comma_separator', 'MM_DD_YYYY', false, true, true, 4, NOW()),

-- Cài đặt cho Jane Smith (user_id = 3)
(3, 'vi', 'dot_separator', 'DD_MM_YYYY', true, false, true, 7, NOW()),

-- Cài đặt cho Google User 1 (user_id = 4)
(4, 'vi', 'dot_separator', 'DD_MM_YYYY', false, false, true, 10, NOW()),

-- Cài đặt cho Google User 2 (user_id = 5)
(5, 'vi', 'dot_separator', 'DD_MM_YYYY', true, true, false, 12, NOW()),

-- Cài đặt cho Facebook User (user_id = 8)
(8, 'vi', 'dot_separator', 'DD_MM_YYYY', false, true, true, 14, NOW()),

-- Cài đặt cho GitHub User (user_id = 9)
(9, 'en', 'comma_separator', 'YYYY_MM_DD', true, true, true, 16, NOW());

-- ===============================================
-- THÔNG TIN TỔNG QUAN DỮ LIỆU MẪU
-- ===============================================

/*
TỔNG QUAN DỮ LIỆU ĐÃ TẠO:

1. USERS (10 users):
   - 4 users đăng ký LOCAL (admin, johndoe, janesmith, newuser)
   - 2 users đăng nhập GOOGLE OAuth2 (googleuser1, googleuser2)
   - 1 user từ FACEBOOK (facebookuser)
   - 1 user từ GITHUB (githubuser)
   - 1 user INACTIVE (inactiveuser)
   - 1 user LOCKED (lockeduser)

2. WALLETS (17 wallets):
   - Các loại ví đa dạng: tiền mặt, ngân hàng, đầu tư, thẻ tín dụng
   - Số dư từ âm (nợ thẻ tín dụng) đến 50 triệu VND
   - Mô tả rõ ràng mục đích sử dụng

3. USER_SETTINGS (7 settings):
   - Ngôn ngữ: Tiếng Việt và Tiếng Anh
   - Định dạng tiền tệ: dấu chấm và dấu phẩy
   - Định dạng ngày: DD/MM/YYYY, MM/DD/YYYY, YYYY/MM/DD
   - Báo cáo: daily/weekly/monthly tùy chọn
   - Ví mặc định được thiết lập

LƯU Ý:
- Mật khẩu mã hóa BCrypt: "password123"
- Các user OAuth2 có provider_id thực tế
- Avatar URL từ các dịch vụ thực tế
- Dữ liệu phù hợp với thị trường Việt Nam
*/

-- Kiểm tra dữ liệu đã tạo
SELECT 'USERS' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'WALLETS', COUNT(*) FROM wallets
UNION ALL
SELECT 'USER_SETTINGS', COUNT(*) FROM user_settings;
