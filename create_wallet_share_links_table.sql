-- Create wallet_share_links table for link sharing functionality
CREATE TABLE `wallet_share_links` (
    `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `wallet_id` INT UNSIGNED NOT NULL,
    `owner_id` INT UNSIGNED NOT NULL,
    `share_token` VARCHAR(255) UNIQUE NOT NULL,
    `permission_level` ENUM('VIEW', 'EDIT', 'ADMIN') NOT NULL,
    `expiry_date` TIMESTAMP NULL,
    `is_active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`wallet_id`) REFERENCES `wallets`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`owner_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
);
