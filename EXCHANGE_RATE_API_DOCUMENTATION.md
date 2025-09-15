# EXCHANGE RATE & TRANSFER MONEY API DOCUMENTATION

## Tổng quan
Hệ thống quản lý tỷ giá động và chuyển tiền đa loại tiền tệ với tỷ giá theo VND.

## Tính năng chính

### 1. Quản lý tỷ giá
- Tỷ giá theo VND làm base currency (1 đơn vị tiền tệ = X VND)
- Có thể thay đổi tỷ giá động
- Hỗ trợ 5 loại tiền tệ: VND, USD, EUR, JPY, GBP
- Tự động khởi tạo tỷ giá mặc định khi start ứng dụng

### 2. Chuyển tiền đa currency
- Chuyển tiền giữa các ví cùng currency
- Chuyển tiền giữa các ví khác currency (tự động chuyển đổi)
- Validate số dư theo USD để đảm bảo tính chính xác
- Hiển thị tỷ giá khi chuyển đổi

## API Endpoints

### Exchange Rate Management

#### 1. Lấy tất cả tỷ giá
```
GET /api/exchange-rates
```

**Response:**
```json
{
    "success": true,
    "message": "Lấy danh sách tỷ giá thành công",
    "data": [
        {
            "id": 1,
            "currency": "USD",
            "rateToVND": 24500.00000000,
            "lastUpdated": "2025-01-15T10:30:00",
            "updatedBy": "SYSTEM"
        },
        {
            "id": 2,
            "currency": "EUR",
            "rateToVND": 26800.00000000,
            "lastUpdated": "2025-01-15T10:30:00",
            "updatedBy": "SYSTEM"
        }
    ]
}
```

#### 2. Lấy tỷ giá theo currency
```
GET /api/exchange-rates/{currency}
```

**Example:**
```
GET /api/exchange-rates/USD
```

**Response:**
```json
{
    "success": true,
    "message": "Lấy tỷ giá thành công",
    "data": {
        "id": 1,
        "currency": "USD",
        "rateToVND": 24500.00000000,
        "lastUpdated": "2025-01-15T10:30:00",
        "updatedBy": "SYSTEM"
    }
}
```

#### 3. Cập nhật tỷ giá (ADMIN only)
```
PUT /api/exchange-rates
Authorization: Bearer {admin_token}
```

**Request Body:**
```json
{
    "currency": "USD",
    "rateToVND": 25000.00
}
```

**Response:**
```json
{
    "success": true,
    "message": "Cập nhật tỷ giá thành công",
    "data": {
        "id": 1,
        "currency": "USD",
        "rateToVND": 25000.00000000,
        "lastUpdated": "2025-01-15T11:00:00",
        "updatedBy": "admin@example.com"
    }
}
```

### Wallet Transfer

#### 1. Chuyển tiền
```
POST /api/wallets/transfer
Authorization: Bearer {user_token}
```

**Request Body:**
```json
{
    "fromWalletId": 1,
    "toWalletId": 2,
    "amount": 100.00
}
```

**Response (cùng currency):**
```json
{
    "success": true,
    "message": "Chuyển tiền thành công: 100.00 USD",
    "fromWalletBalance": 900.00,
    "toWalletBalance": 1100.00,
    "transferTime": "2025-01-15T11:30:00",
    "success": true
}
```

**Response (khác currency - USD to VND):**
```json
{
    "success": true,
    "message": "Chuyển tiền thành công: 100.00 USD → 2450000.00 VND (tỷ giá: 1 USD = 24500.0000 VND)",
    "fromWalletBalance": 900.00,
    "toWalletBalance": 27450000.00,
    "transferTime": "2025-01-15T11:30:00",
    "success": true
}
```

## Tỷ giá mặc định

| Currency | Rate to VND |
|----------|-------------|
| VND      | 1.00        |
| USD      | 24,500.00   |
| EUR      | 26,800.00   |
| JPY      | 165.00      |
| GBP      | 30,200.00   |

## Validation Rules

### Transfer Money
1. **Số dư không đủ**: Kiểm tra theo USD để đảm bảo tính chính xác
2. **Cùng ví**: Không thể chuyển tiền trong cùng một ví
3. **Quyền sở hữu**: Chỉ chuyển được giữa các ví của chính mình
4. **Currency hỗ trợ**: Chỉ hỗ trợ 5 loại tiền tệ đã định nghĩa

### Exchange Rate
1. **Admin only**: Chỉ ADMIN mới có thể cập nhật tỷ giá
2. **Rate > 0**: Tỷ giá phải lớn hơn 0
3. **Currency hợp lệ**: Chỉ chấp nhận các currency trong enum

## Examples

### Ví dụ 1: Chuyển 1,000,000 VND sang ví USD
```bash
curl -X POST "http://localhost:8080/api/wallets/transfer" \
-H "Authorization: Bearer {token}" \
-H "Content-Type: application/json" \
-d '{
    "fromWalletId": 1,
    "toWalletId": 2,
    "amount": 1000000.00
}'
```

Kết quả: 1,000,000 VND → ~40.82 USD (với tỷ giá 1 USD = 24,500 VND)

### Ví dụ 2: Cập nhật tỷ giá USD
```bash
curl -X PUT "http://localhost:8080/api/exchange-rates" \
-H "Authorization: Bearer {admin_token}" \
-H "Content-Type: application/json" \
-d '{
    "currency": "USD",
    "rateToVND": 25000.00
}'
```

### Ví dụ 3: Xem tổng số dư tất cả ví
```bash
curl -X GET "http://localhost:8080/api/wallets/summary" \
-H "Authorization: Bearer {token}"
```

## Database Schema

### exchange_rates table
```sql
CREATE TABLE exchange_rates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    currency VARCHAR(10) NOT NULL UNIQUE,
    rate_to_vnd DECIMAL(19,8) NOT NULL,
    last_updated DATETIME NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);
```

## Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | Số dư không đủ | Insufficient balance |
| 400 | Không thể chuyển tiền trong cùng một ví | Same wallet transfer |
| 404 | Không tìm thấy ví | Wallet not found |
| 403 | Không có quyền truy cập | Access denied |
| 400 | Loại tiền tệ không được hỗ trợ | Unsupported currency |
