-- Tạo bảng exchange_rates
CREATE TABLE IF NOT EXISTS exchange_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    currency VARCHAR(10) NOT NULL UNIQUE,
    rate_to_vnd DECIMAL(19,8) NOT NULL,
    last_updated DATETIME NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    INDEX idx_currency (currency),
    INDEX idx_last_updated (last_updated)
);

-- Thêm dữ liệu tỷ giá mặc định (1 đơn vị tiền tệ = ? VND)
INSERT INTO exchange_rates (currency, rate_to_vnd, last_updated, updated_by) VALUES
('VND', 1.00000000, NOW(), 'SYSTEM'),
('USD', 24500.00000000, NOW(), 'SYSTEM'),
('EUR', 26800.00000000, NOW(), 'SYSTEM'),
('JPY', 165.00000000, NOW(), 'SYSTEM'),
('GBP', 30200.00000000, NOW(), 'SYSTEM')
ON DUPLICATE KEY UPDATE
    rate_to_vnd = VALUES(rate_to_vnd),
    last_updated = NOW(),
    updated_by = 'SYSTEM';
