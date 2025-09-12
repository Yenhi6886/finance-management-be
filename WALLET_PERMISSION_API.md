# API Quản Lý Quyền Ví (Wallet Permission API)

## Tổng quan
API này cho phép quản lý quyền truy cập chi tiết cho các ví được chia sẻ, với hệ thống quyền granular và linh hoạt.

## Các loại quyền (Permission Types)

### **Quyền xem:**
- `VIEW_WALLET` - Xem thông tin ví
- `VIEW_BALANCE` - Xem số dư
- `VIEW_TRANSACTIONS` - Xem giao dịch

### **Quyền chỉnh sửa:**
- `EDIT_WALLET` - Chỉnh sửa thông tin ví
- `ADD_TRANSACTION` - Thêm giao dịch
- `EDIT_TRANSACTION` - Chỉnh sửa giao dịch
- `DELETE_TRANSACTION` - Xóa giao dịch

### **Quyền quản trị:**
- `MANAGE_PERMISSIONS` - Quản lý quyền truy cập
- `SHARE_WALLET` - Chia sẻ ví với người khác
- `DELETE_WALLET` - Xóa ví
- `TRANSFER_OWNERSHIP` - Chuyển quyền sở hữu

### **Quyền báo cáo:**
- `VIEW_REPORTS` - Xem báo cáo
- `EXPORT_DATA` - Xuất dữ liệu

## Các Endpoint

### 1. Gán quyền cho user
**POST** `/api/wallet-permissions/{walletId}/users/{userId}`

**Request Body:**
```json
{
  "permissions": [
    "VIEW_WALLET",
    "VIEW_BALANCE", 
    "VIEW_TRANSACTIONS",
    "ADD_TRANSACTION"
  ],
  "reason": "Gán quyền cơ bản cho user mới"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Gán quyền thành công",
  "data": [
    {
      "id": 1,
      "walletShareId": 1,
      "permissionType": "VIEW_WALLET",
      "permissionDisplayName": "Xem thông tin ví",
      "isGranted": true,
      "grantedBy": 1,
      "grantedAt": "2024-01-01T10:00:00",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00"
    }
  ]
}
```

### 2. Lấy quyền của user với ví
**GET** `/api/wallet-permissions/{walletId}/users/{userId}`

**Response:**
```json
{
  "success": true,
  "message": "Lấy danh sách quyền thành công",
  "data": [
    {
      "id": 1,
      "walletShareId": 1,
      "permissionType": "VIEW_WALLET",
      "permissionDisplayName": "Xem thông tin ví",
      "isGranted": true,
      "grantedBy": 1,
      "grantedAt": "2024-01-01T10:00:00"
    }
  ]
}
```

### 3. Lấy tất cả quyền ví của tôi
**GET** `/api/wallet-permissions/my-permissions`

**Response:**
```json
{
  "success": true,
  "message": "Lấy danh sách quyền ví của tôi thành công",
  "data": [
    {
      "walletId": 1,
      "walletName": "Ví chính",
      "ownerName": "Nguyễn Văn A",
      "ownerEmail": "owner@example.com",
      "permissions": ["VIEW_WALLET", "VIEW_BALANCE", "VIEW_TRANSACTIONS"],
      "permissionDisplayNames": ["Xem thông tin ví", "Xem số dư", "Xem giao dịch"],
      "sharedAt": "2024-01-01T10:00:00",
      "isActive": true
    }
  ]
}
```

### 4. Thu hồi quyền cụ thể
**DELETE** `/api/wallet-permissions/{walletId}/users/{userId}/permissions/{permissionType}`

**Response:**
```json
{
  "success": true,
  "message": "Thu hồi quyền thành công",
  "data": null
}
```

### 5. Kiểm tra quyền
**GET** `/api/wallet-permissions/{walletId}/users/{userId}/has-permission/{permissionType}`

**Response:**
```json
{
  "success": true,
  "message": "Kiểm tra quyền thành công",
  "data": true
}
```

## Quyền mặc định theo Permission Level

### **VIEW Level:**
- `VIEW_WALLET`
- `VIEW_BALANCE`
- `VIEW_TRANSACTIONS`

### **EDIT Level:**
- Tất cả quyền VIEW +
- `EDIT_WALLET`
- `ADD_TRANSACTION`
- `EDIT_TRANSACTION`
- `DELETE_TRANSACTION`
- `VIEW_REPORTS`

### **ADMIN Level:**
- Tất cả quyền EDIT +
- `MANAGE_PERMISSIONS`
- `SHARE_WALLET`
- `VIEW_REPORTS`
- `EXPORT_DATA`

## Test Cases

### **Test 1: Gán quyền cơ bản**
```bash
curl -X POST http://localhost:8080/api/wallet-permissions/1/users/2 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "permissions": ["VIEW_WALLET", "VIEW_BALANCE", "VIEW_TRANSACTIONS"],
    "reason": "Gán quyền xem cơ bản"
  }'
```

### **Test 2: Gán quyền chỉnh sửa**
```bash
curl -X POST http://localhost:8080/api/wallet-permissions/1/users/2 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "permissions": [
      "VIEW_WALLET", "VIEW_BALANCE", "VIEW_TRANSACTIONS",
      "EDIT_WALLET", "ADD_TRANSACTION", "EDIT_TRANSACTION"
    ],
    "reason": "Nâng cấp quyền chỉnh sửa"
  }'
```

### **Test 3: Thu hồi quyền**
```bash
curl -X DELETE http://localhost:8080/api/wallet-permissions/1/users/2/permissions/ADD_TRANSACTION \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### **Test 4: Kiểm tra quyền**
```bash
curl -X GET http://localhost:8080/api/wallet-permissions/1/users/2/has-permission/VIEW_WALLET \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Lưu ý

1. **Chỉ chủ sở hữu ví** mới có thể gán/thu hồi quyền
2. **Quyền được gán tự động** khi chia sẻ ví theo Permission Level
3. **Có thể gán quyền riêng lẻ** không theo level mặc định
4. **Thu hồi quyền** sẽ set `isGranted = false` thay vì xóa
5. **Kiểm tra quyền** trả về `true/false` để sử dụng trong business logic
