# Test API Share Ví

## Lỗi gặp phải:
```
Data truncated for column 'permission_level' at row 1
```

## Nguyên nhân:
- Database có cột `permission_level` với ENUM('VIEW', 'EDIT', 'ADMIN')
- JPA có thể không map đúng enum từ Java sang database

## Cách sửa:

### 1. Chạy script SQL để sửa database:
```sql
-- Chạy file fix_permission_level.sql
```

### 2. Kiểm tra entity WalletShare:
- Đã thêm `@Column(name = "permission_level")` 
- Đã có `@Enumerated(EnumType.STRING)`

### 3. Test API:

#### Request:
```json
POST /api/wallet-shares
{
  "walletId": 6,
  "email": "chimbuon3@gmail.com",
  "permissionLevel": "VIEW",
  "message": "Mời bạn cùng quản lý ví này"
}
```

#### Expected Response:
```json
{
  "success": true,
  "message": "Chia sẻ ví thành công. Email thông báo đã được gửi đến người nhận.",
  "data": {
    "id": 1,
    "walletId": 6,
    "walletName": "Ví chính",
    "ownerName": "Nguyễn Văn A",
    "sharedWithEmail": "chimbuon3@gmail.com",
    "sharedWithName": "Chim Buồn",
    "permissionLevel": "VIEW",
    "isActive": true,
    "createdAt": "2024-01-01T00:00:00"
  }
}
```

## Các bước debug:

1. **Kiểm tra database schema:**
   ```sql
   DESCRIBE wallet_shares;
   ```

2. **Kiểm tra dữ liệu hiện tại:**
   ```sql
   SELECT * FROM wallet_shares;
   ```

3. **Kiểm tra log Hibernate:**
   - Bật `spring.jpa.show-sql=true`
   - Xem SQL query được generate

4. **Test với các giá trị khác:**
   - `"permissionLevel": "EDIT"`
   - `"permissionLevel": "ADMIN"`

## Nếu vẫn lỗi:

1. **Xóa và tạo lại bảng:**
   ```sql
   DROP TABLE wallet_permissions;
   DROP TABLE wallet_shares;
   -- Chạy lại script tạo bảng
   ```

2. **Kiểm tra JPA configuration:**
   - Đảm bảo `hibernate.ddl-auto` không conflict
   - Kiểm tra dialect MySQL

3. **Restart application** sau khi sửa database
