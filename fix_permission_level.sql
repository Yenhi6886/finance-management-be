-- Script để sửa lỗi permission_level trong database
-- Chạy script này nếu gặp lỗi "Data truncated for column 'permission_level'"

USE finance;

-- Kiểm tra cấu trúc hiện tại của bảng wallet_shares
DESCRIBE wallet_shares;

-- Nếu cần thiết, xóa và tạo lại bảng với cấu trúc đúng
DROP TABLE IF EXISTS wallet_permissions;
DROP TABLE IF EXISTS wallet_shares;

-- Tạo lại bảng wallet_shares với cấu trúc đúng
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

-- Tạo lại bảng wallet_permissions
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
    `granted_by` INT UNSIGNED NOT NULL,
    `granted_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE(`wallet_share_id`, `permission_type`),
    FOREIGN KEY (`wallet_share_id`) REFERENCES `wallet_shares`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`granted_by`) REFERENCES `users`(`id`) ON DELETE CASCADE
);

-- Kiểm tra lại cấu trúc
DESCRIBE wallet_shares;
DESCRIBE wallet_permissions;
