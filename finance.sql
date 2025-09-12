create database finance;
use finance;

-- =================================================================
-- Bảng Người dùng (Users) - Đáp ứng yêu cầu 1-9
-- =================================================================
CREATE TABLE `users` (
                         `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         `username` VARCHAR(50) NOT NULL UNIQUE,
                         `email` VARCHAR(100) NOT NULL UNIQUE,
                         `password` VARCHAR(255) NOT NULL,
                         `first_name` VARCHAR(50) NOT NULL,
                         `last_name` VARCHAR(50) NOT NULL,
                         `avatar_url` VARCHAR(255) DEFAULT NULL,
                         `phone_number` VARCHAR(20) DEFAULT NULL,
                         `status` ENUM('INACTIVE', 'ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'INACTIVE',
                         `auth_provider` ENUM('LOCAL', 'GOOGLE', 'FACEBOOK', 'GITHUB') NOT NULL DEFAULT 'LOCAL',
                         `provider_id` VARCHAR(255) DEFAULT NULL,
                         `activation_token` VARCHAR(255) DEFAULT NULL,
                         `reset_password_token` VARCHAR(255) DEFAULT NULL,
                         `reset_password_expires` DATETIME DEFAULT NULL,
                         `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         `deleted_at` TIMESTAMP NULL DEFAULT NULL -- Dành cho chức năng xoá mềm (soft delete)
);

-- =================================================================
-- Bảng Tài khoản mạng xã hội (Social Accounts) - Đáp ứng yêu cầu 3
-- =================================================================
CREATE TABLE `social_accounts` (
                                   `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                   `user_id` INT UNSIGNED NOT NULL,
                                   `provider` VARCHAR(50) NOT NULL, -- 'google', 'facebook', 'github'
                                   `provider_user_id` VARCHAR(255) NOT NULL,
                                   `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   UNIQUE(`provider`, `provider_user_id`),
                                   FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- =================================================================
-- Bảng Ví tiền (Wallets) - Đáp ứng yêu cầu 10-13, 16-21
-- =================================================================
CREATE TABLE `wallets` (
                           `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                           `user_id` INT UNSIGNED NOT NULL, -- Chủ sở hữu ví
                           `name` VARCHAR(100) NOT NULL,
                           `icon` VARCHAR(255) DEFAULT NULL,
                           `currency` ENUM('VND', 'USD', 'EUR', 'JPY', 'GBP', 'AUD', 'CAD', 'CHF', 'CNY', 'KRW') NOT NULL DEFAULT 'VND',
                           `initial_balance` DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
                           `description` TEXT DEFAULT NULL,
                           `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- =================================================================
-- Bảng Chia sẻ Ví (Wallet Shares) - Đáp ứng yêu cầu 13-15
-- =================================================================
CREATE TABLE `wallet_shares` (
                                 `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                 `wallet_id` INT UNSIGNED NOT NULL,
                                 `owner_id` INT UNSIGNED NOT NULL,
                                 `shared_with_user_id` INT UNSIGNED NOT NULL,
                                 `permission_level` ENUM('VIEW', 'EDIT', 'ADMIN') NOT NULL DEFAULT 'VIEW',
                                 `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
                                 `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 UNIQUE(`wallet_id`, `shared_with_user_id`),
                                 FOREIGN KEY (`wallet_id`) REFERENCES `wallets`(`id`) ON DELETE CASCADE,
                                 FOREIGN KEY (`owner_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                                 FOREIGN KEY (`shared_with_user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- =================================================================
-- Bảng Quyền Ví (Wallet Permissions) - Quản lý quyền chi tiết
-- =================================================================
CREATE TABLE `wallet_permissions` (
                                      `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                      `wallet_share_id` INT UNSIGNED NOT NULL,
                                      `permission_type` ENUM(
                                          'VIEW_WALLET', 'VIEW_BALANCE', 'VIEW_TRANSACTIONS',
                                          'EDIT_WALLET', 'ADD_TRANSACTION', 'EDIT_TRANSACTION', 'DELETE_TRANSACTION',
                                          'MANAGE_PERMISSIONS', 'SHARE_WALLET', 'DELETE_WALLET', 'TRANSFER_OWNERSHIP',
                                          'VIEW_REPORTS', 'EXPORT_DATA'
                                      ) NOT NULL,
                                      `is_granted` BOOLEAN NOT NULL DEFAULT TRUE,
                                      `granted_by` INT UNSIGNED DEFAULT NULL,
                                      `granted_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      FOREIGN KEY (`wallet_share_id`) REFERENCES `wallet_shares`(`id`) ON DELETE CASCADE,
                                      FOREIGN KEY (`granted_by`) REFERENCES `users`(`id`) ON DELETE SET NULL,
                                      UNIQUE(`wallet_share_id`, `permission_type`)
);

-- =================================================================
-- Bảng Danh mục (Categories) - Đáp ứng yêu cầu 31-35
-- =================================================================
CREATE TABLE `categories` (
                              `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                              `user_id` INT UNSIGNED DEFAULT NULL, -- NULL nghĩa là danh mục mặc định của hệ thống
                              `name` VARCHAR(100) NOT NULL,
                              `notes` TEXT DEFAULT NULL,
                              `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =================================================================
-- Bảng Giao dịch (Transactions) - Đáp ứng yêu cầu 21-28, 35, 41-44
-- =================================================================
CREATE TABLE `transactions` (
                                `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                `wallet_id` INT UNSIGNED NOT NULL,
                                `category_id` INT UNSIGNED NOT NULL,
                                `amount` DECIMAL(15, 2) NOT NULL,
                                `type` ENUM('income', 'expense', 'transfer') NOT NULL, -- Thu, Chi, Chuyển khoản
                                `notes` TEXT DEFAULT NULL,
                                `transaction_date` DATETIME NOT NULL,
                                `related_transaction_id` INT UNSIGNED DEFAULT NULL, -- Dùng để liên kết 2 giao dịch khi chuyển tiền giữa các ví
                                `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                FOREIGN KEY (`wallet_id`) REFERENCES `wallets`(`id`) ON DELETE CASCADE,
                                FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`),
                                FOREIGN KEY (`related_transaction_id`) REFERENCES `transactions`(`id`) ON DELETE SET NULL
);

-- =================================================================
-- Bảng Ngân sách (Budgets) - Đáp ứng yêu cầu 36-40
-- =================================================================
CREATE TABLE `budgets` (
                           `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                           `user_id` INT UNSIGNED NOT NULL,
                           `category_id` INT UNSIGNED NOT NULL,
                           `amount` DECIMAL(15, 2) NOT NULL,
                           `start_date` DATE NOT NULL,
                           `end_date` DATE NOT NULL,
                           `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           UNIQUE(`user_id`, `category_id`, `start_date`),
                           FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                           FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE CASCADE
);


-- =================================================================
-- Bảng Cài đặt Người dùng (User Settings) - Đáp ứng yêu cầu 30, 45-48
-- =================================================================
CREATE TABLE `user_settings` (
                                 `user_id` INT UNSIGNED PRIMARY KEY,
                                 `language` VARCHAR(10) DEFAULT 'vi', -- 'vi', 'en'
                                 `currency_format` ENUM('dot_separator', 'comma_separator') DEFAULT 'dot_separator', -- 1.000.000 vs 1,000,000
                                 `date_format` ENUM('DD/MM/YYYY', 'MM/DD/YYYY', 'YYYY/MM/DD') DEFAULT 'DD/MM/YYYY',
                                 `daily_report` BOOLEAN DEFAULT FALSE,
                                 `weekly_report` BOOLEAN DEFAULT FALSE,
                                 `monthly_report` BOOLEAN DEFAULT FALSE,
                                 `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- =================================================================
-- Thêm INDEX để tăng tốc độ truy vấn
-- =================================================================
ALTER TABLE `users` ADD INDEX `idx_username` (`username`);
ALTER TABLE `users` ADD INDEX `idx_email` (`email`);
ALTER TABLE `wallets` ADD INDEX `idx_user_id` (`user_id`);
ALTER TABLE `transactions` ADD INDEX `idx_wallet_id` (`wallet_id`);
ALTER TABLE `transactions` ADD INDEX `idx_category_id` (`category_id`);
ALTER TABLE `transactions` ADD INDEX `idx_transaction_date` (`transaction_date`);
ALTER TABLE `budgets` ADD INDEX `idx_user_category` (`user_id`, `category_id`);