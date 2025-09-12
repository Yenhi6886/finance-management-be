-- Dữ liệu mẫu cho bảng users
INSERT INTO users (email, username, password, first_name, last_name, phone_number, avatar_url, status, auth_provider, provider_id, created_at, updated_at) VALUES
('john.doe@gmail.com', 'johndoe123', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Doe', '0901234567', 'https://example.com/avatar1.jpg', 'ACTIVE', 'LOCAL', NULL, '2024-01-15 10:30:00', '2024-01-15 10:30:00'),
('jane.smith@gmail.com', 'janesmith456', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jane', 'Smith', '0987654321', 'https://example.com/avatar2.jpg', 'ACTIVE', 'GOOGLE', 'google_12345', '2024-01-20 14:15:00', '2024-01-20 14:15:00'),
('bob.wilson@outlook.com', 'bobwilson789', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Bob', 'Wilson', '0912345678', NULL, 'ACTIVE', 'LOCAL', NULL, '2024-02-01 09:45:00', '2024-02-01 09:45:00'),
('alice.brown@yahoo.com', 'alicebrown321', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Alice', 'Brown', '0934567890', 'https://example.com/avatar3.jpg', 'ACTIVE', 'GOOGLE', 'google_67890', '2024-02-10 16:20:00', '2024-02-10 16:20:00'),
('charlie.davis@gmail.com', 'charliedavis', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Charlie', 'Davis', '0945678901', NULL, 'INACTIVE', 'LOCAL', NULL, '2024-02-15 11:10:00', '2024-02-15 11:10:00');

-- Dữ liệu mẫu cho bảng wallets
INSERT INTO wallets (name, icon, currency, initial_balance, description, user_id, created_at, updated_at) VALUES
-- Ví của John Doe (user_id = 1)
('Ví tiền mặt', '💵', 'VND', 5000000.0000, 'Ví để chứa tiền mặt hàng ngày', 1, '2024-01-15 10:35:00', '2024-01-15 10:35:00'),
('Tài khoản ngân hàng', '🏦', 'VND', 25000000.0000, 'Tài khoản chính tại Vietcombank', 1, '2024-01-15 10:40:00', '2024-01-15 10:40:00'),
('Ví tiết kiệm', '💰', 'VND', 50000000.0000, 'Tài khoản tiết kiệm dài hạn', 1, '2024-01-16 08:20:00', '2024-01-16 08:20:00'),

-- Ví của Jane Smith (user_id = 2)
('Cash Wallet', '💸', 'USD', 500.0000, 'Daily spending money', 2, '2024-01-20 14:20:00', '2024-01-20 14:20:00'),
('Bank Account', '🏛️', 'USD', 5000.0000, 'Main checking account at Chase Bank', 2, '2024-01-20 14:25:00', '2024-01-20 14:25:00'),
('Investment Fund', '📈', 'USD', 15000.0000, 'Long term investment portfolio', 2, '2024-01-21 09:30:00', '2024-01-21 09:30:00'),

-- Ví của Bob Wilson (user_id = 3)
('Ví tiền xu', '🪙', 'VND', 2000000.0000, 'Ví đựng tiền lẻ', 3, '2024-02-01 09:50:00', '2024-02-01 09:50:00'),
('Tài khoản lương', '💼', 'VND', 12000000.0000, 'Tài khoản nhận lương hàng tháng', 3, '2024-02-01 10:00:00', '2024-02-01 10:00:00'),

-- Ví của Alice Brown (user_id = 4)
('Euro Wallet', '💶', 'EUR', 800.0000, 'For European travels', 4, '2024-02-10 16:25:00', '2024-02-10 16:25:00'),
('Main Account', '🏪', 'VND', 8000000.0000, 'Tài khoản chính', 4, '2024-02-10 16:30:00', '2024-02-10 16:30:00'),
('Emergency Fund', '🚨', 'VND', 20000000.0000, 'Quỹ dự phòng khẩn cấp', 4, '2024-02-11 10:15:00', '2024-02-11 10:15:00'),

-- Ví của Charlie Davis (user_id = 5) - user inactive nhưng vẫn có ví
('Old Wallet', '👛', 'VND', 1000000.0000, 'Ví cũ không dùng', 5, '2024-02-15 11:15:00', '2024-02-15 11:15:00');

-- Dữ liệu mẫu cho bảng user_settings
INSERT INTO user_settings (user_id, language, currency_format, date_format, daily_report, weekly_report, monthly_report, current_wallet_id, updated_at) VALUES
-- Settings cho John Doe (user_id = 1) - chọn ví tiền mặt làm ví chính
(1, 'vi', 'dot_separator', 'DD_MM_YYYY', true, true, false, 1, '2024-01-15 11:00:00'),

-- Settings cho Jane Smith (user_id = 2) - chọn bank account làm ví chính
(2, 'en', 'comma_separator', 'MM_DD_YYYY', false, true, true, 5, '2024-01-20 15:00:00'),

-- Settings cho Bob Wilson (user_id = 3) - chọn tài khoản lương làm ví chính
(3, 'vi', 'dot_separator', 'DD_MM_YYYY', true, false, true, 8, '2024-02-01 11:00:00'),

-- Settings cho Alice Brown (user_id = 4) - chọn main account làm ví chính
(4, 'vi', 'comma_separator', 'YYYY_MM_DD', false, false, true, 10, '2024-02-10 17:00:00'),

-- Settings cho Charlie Davis (user_id = 5) - user inactive
(5, 'vi', 'dot_separator', 'DD_MM_YYYY', false, false, false, 12, '2024-02-15 12:00:00');
