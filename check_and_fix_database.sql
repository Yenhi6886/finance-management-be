-- Script kiểm tra và sửa lỗi permission_level
USE finance;

-- 1. Kiểm tra cấu trúc hiện tại
DESCRIBE wallet_shares;

-- 2. Kiểm tra dữ liệu hiện tại
SELECT * FROM wallet_shares;

-- 3. Kiểm tra cấu trúc cột permission_level
SHOW COLUMNS FROM wallet_shares LIKE 'permission_level';

-- 4. Nếu cột permission_level có vấn đề, sửa lại
-- (Chạy lệnh này nếu cần thiết)
ALTER TABLE wallet_shares 
MODIFY COLUMN permission_level ENUM('VIEW', 'EDIT', 'ADMIN') NOT NULL DEFAULT 'VIEW';

-- 5. Kiểm tra lại sau khi sửa
DESCRIBE wallet_shares;

-- 6. Test insert thủ công
INSERT INTO wallet_shares (wallet_id, owner_id, shared_with_user_id, permission_level, is_active) 
VALUES (6, 1, 2, 'EDIT', TRUE);

-- 7. Xóa record test
DELETE FROM wallet_shares WHERE wallet_id = 6 AND shared_with_user_id = 2;
