# Hệ Thống Đa Ngôn Ngữ (Internationalization - i18n)

## Tổng Quan
Hệ thống đã được cấu hình để hỗ trợ đa ngôn ngữ với tiếng Việt (mặc định) và tiếng Anh.

## Cấu Trúc Files

### 1. Configuration Files
- `InternationalizationConfig.java` - Cấu hình chính cho i18n
- `MessageService.java` - Service để lấy messages theo locale

### 2. Message Files
- `messages.properties` - Messages mặc định (tiếng Việt)
- `messages_vi.properties` - Messages tiếng Việt
- `messages_en.properties` - Messages tiếng Anh

### 3. Updated Components
- `GlobalExceptionHandler.java` - Sử dụng MessageService
- Tất cả DTO Request classes - Sử dụng message keys thay vì hardcoded strings
- `EmailService.java` - Sử dụng message keys cho email templates
- `JwtUtil.java` - Sử dụng message keys cho log messages
- `WalletPermissionAspect.java` - Sử dụng message keys cho permission messages

## Cách Sử Dụng

### 1. Thay Đổi Ngôn Ngữ qua Header
```http
GET /api/test/locale
Accept-Language: en

GET /api/test/locale
Accept-Language: vi
```

### 2. Thay Đổi Ngôn Ngữ qua Parameter
```http
GET /api/test/locale?lang=en
GET /api/test/locale?lang=vi
```

### 3. Sử Dụng MessageService trong Code
```java
@Autowired
private MessageService messageService;

// Lấy message đơn giản
String message = messageService.getMessage("validation.notblank.email");

// Lấy message với parameters
String message = messageService.getMessage("wallet.share.invitation.subject", 
    new Object[]{"John Doe", "My Wallet"});

// Lấy message với locale cụ thể
String message = messageService.getMessage("success", null, Locale.ENGLISH);
```

## Message Keys Available

### Validation Messages
- `validation.notblank.email` - Email validation
- `validation.notblank.password` - Password validation
- `validation.notblank.username` - Username validation
- `validation.size.username` - Username size validation
- `validation.size.password` - Password size validation
- `validation.email.invalid` - Invalid email format
- Và nhiều message keys khác...

### Exception Messages
- `exception.resource.not.found` - Resource not found
- `exception.bad.request` - Bad request
- `exception.insufficient.balance` - Insufficient balance
- `exception.wallet.not.found` - Wallet not found
- Và nhiều message keys khác...

### Business Logic Messages
- `wallet.share.invitation.subject` - Wallet sharing invitation subject
- `auth.bad.credentials` - Bad credentials
- `jwt.invalid.token` - Invalid JWT token
- Và nhiều message keys khác...

## Testing

### 1. Test Locale Controller
```http
GET /api/test/locale
Accept-Language: en
```

Response sẽ chứa các messages bằng tiếng Anh.

```http
GET /api/test/locale
Accept-Language: vi
```

Response sẽ chứa các messages bằng tiếng Việt.

### 2. Test Validation
Gửi invalid data đến các API endpoints để test validation messages theo ngôn ngữ.

## Thêm Messages Mới

### 1. Thêm vào messages.properties (default)
```properties
new.message.key=Message mặc định bằng tiếng Việt
```

### 2. Thêm vào messages_vi.properties
```properties
new.message.key=Message bằng tiếng Việt
```

### 3. Thêm vào messages_en.properties
```properties
new.message.key=Message in English
```

### 4. Sử dụng trong code
```java
String message = messageService.getMessage("new.message.key");
```

## Lưu Ý Quan Trọng

1. **Locale mặc định**: Tiếng Việt (`vi`)
2. **Encoding**: UTF-8 cho tất cả message files
3. **Parameter placeholders**: Sử dụng `{0}`, `{1}`, etc. cho parameterized messages
4. **Message keys**: Sử dụng trong validation annotations như `@NotBlank(message = "{validation.notblank.email}")`
5. **Fallback**: Nếu không tìm thấy message cho locale hiện tại, sẽ fallback về message mặc định

## API Endpoints Mới

- `GET /api/test/locale` - Test locale và messages
- `GET /api/test/validation-test` - Test validation messages

## Troubleshooting

1. **Messages không thay đổi**: Kiểm tra Accept-Language header hoặc lang parameter
2. **Message key không tìm thấy**: Kiểm tra message key có tồn tại trong file properties không
3. **Encoding issues**: Đảm bảo tất cả files sử dụng UTF-8 encoding
