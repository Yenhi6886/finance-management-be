-- Fix wallet_shares table to allow NULL for shared_with_user_id
-- This is needed for link sharing functionality

-- First, drop the foreign key constraint
ALTER TABLE `wallet_shares` DROP FOREIGN KEY `wallet_shares_ibfk_3`;

-- Modify the column to allow NULL
ALTER TABLE `wallet_shares` MODIFY COLUMN `shared_with_user_id` INT UNSIGNED NULL;

-- Add the new columns for link sharing
ALTER TABLE `wallet_shares` ADD COLUMN `share_token` VARCHAR(255) UNIQUE NULL;
ALTER TABLE `wallet_shares` ADD COLUMN `expires_at` TIMESTAMP NULL;
ALTER TABLE `wallet_shares` ADD COLUMN `message` TEXT NULL;

-- Re-add the foreign key constraint (only when shared_with_user_id is not NULL)
ALTER TABLE `wallet_shares` ADD CONSTRAINT `wallet_shares_ibfk_3` 
    FOREIGN KEY (`shared_with_user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE;

-- Update the unique constraint to handle NULL values
ALTER TABLE `wallet_shares` DROP INDEX `wallet_id`;
ALTER TABLE `wallet_shares` ADD UNIQUE KEY `unique_wallet_user` (`wallet_id`, `shared_with_user_id`);
