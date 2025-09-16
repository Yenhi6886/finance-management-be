# Finance Management Backend

Hệ thống backend quản lý tài chính sử dụng Spring Boot, Java 17, và Gradle.

## Tính năng đã hoàn thành

### ✅ Xác thực và ủy quyền người dùng

1. **Đăng ký tài khoản** - POST `/api/auth/register`
2. **Đăng nhập** - POST `/api/auth/login`
3. **Kích hoạt tài khoản qua email** - GET `/api/auth/activate?token={token}`
4. **Quên mật khẩu** - POST `/api/auth/forgot-password`
5. **Đặt lại mật khẩu** - POST `/api/auth/reset-password`
6. **Đổi mật khẩu** - POST `/api/user/change-password`
7. **Cập nhật thông tin profile** - PUT `/api/user/profile`
8. **Lấy thông tin profile** - GET `/api/user/profile`
9. **Xóa tài khoản** - DELETE `/api/user/account`

## Công nghệ sử dụng

- **Java 17**
- **Spring Boot 3.5.5**
- **Spring Security 6**
- **Spring Data JPA**
- **MySQL** (database)
- **JWT** (JSON Web Token)
- **Spring Mail** (gửi email)
- **Gradle** (build tool)
- **Lombok** (giảm boilerplate code)

## Cấu trúc dự án

```
src/main/java/com/example/backend/
├── controller/          # REST Controllers
│   ├── AuthController.java
│   └── UserController.java
├── dto/                 # Data Transfer Objects
│   ├── request/         # Request DTOs
│   └── response/        # Response DTOs
├── entity/              # JPA Entities
│   └── User.java
├── enums/               # Enum classes
│   ├── AuthProvider.java
│   └── UserStatus.java
├── mapper/              # Entity-DTO mappers
│   └── UserMapper.java
├── repository/          # JPA Repositories
│   └── UserRepository.java
├── security/            # Security configuration
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityConfig.java
├── service/             # Business logic
│   ├── EmailService.java
│   └── UserService.java
├── util/                # Utility classes
│   └── JwtUtil.java
└── BackendApplication.java
```

## Việc cần làm tiếp theo

### 🔧 Cần hoàn thiện ngay

#### 1. **Đăng nhập bằng mạng xã hội (OAuth2)**

- [ ] Cấu hình OAuth2 cho Google
- [ ] Cấu hình OAuth2 cho Facebook
- [ ] Cấu hình OAuth2 cho GitHub
- [ ] Tạo OAuth2LoginSuccessHandler
- [ ] Cập nhật SecurityConfig để hỗ trợ OAuth2

**Files cần tạo:**

```
security/
├── OAuth2LoginSuccessHandler.java
├── OAuth2UserInfo.java
├── OAuth2UserInfoFactory.java
└── oauth2/
    ├── GoogleOAuth2UserInfo.java
    ├── FacebookOAuth2UserInfo.java
    └── GitHubOAuth2UserInfo.java
```

#### 2. **Logout API**

- [ ] Tạo endpoint logout - POST `/api/auth/logout`
- [ ] Implement JWT blacklist (Redis hoặc in-memory cache)
- [ ] Cập nhật frontend để xóa token khi logout

#### 3. **Security Config hoàn chỉnh**

- [ ] Hoàn thiện SecurityConfig.java
- [ ] Cấu hình CORS chi tiết
- [ ] Cấu hình rate limiting
- [ ] Thêm security headers

#### 4. **Global Exception Handler**

- [ ] Tạo GlobalExceptionHandler.java
- [ ] Xử lý các exception cụ thể
- [ ] Chuẩn hóa error response format

#### 5. **Application Properties**

- [ ] Cấu hình database connection
- [ ] Cấu hình email SMTP
- [ ] Cấu hình JWT secret và expiration
- [ ] Cấu hình OAuth2 client credentials

### 🚀 Tính năng mở rộng

#### 6. **Validation và Error Handling nâng cao**

- [ ] Custom validation annotations
- [ ] Validation cho phone number format
- [ ] Password strength validation
- [ ] Email format validation nâng cao

#### 7. **Role-based Authorization**

- [ ] Tạo Role entity
- [ ] Tạo UserRole entity (many-to-many)
- [ ] Cập nhật User entity để support roles
- [ ] Implement role-based access control

#### 8. **Audit và Logging**

- [ ] Tạo AuditLog entity
- [ ] Implement audit trail cho user actions
- [ ] Cấu hình logging levels
- [ ] Tạo log cho security events

#### 9. **API Documentation**

- [ ] Thêm Swagger/OpenAPI 3
- [ ] Document tất cả endpoints
- [ ] Tạo API examples

#### 10. **Testing**

- [ ] Unit tests cho Services
- [ ] Integration tests cho Controllers
- [ ] Security tests
- [ ] Database tests với TestContainers

#### 11. **Performance và Monitoring**

- [ ] Thêm Spring Boot Actuator
- [ ] Cấu hình metrics và health checks
- [ ] Implement caching (Redis)
- [ ] Database connection pooling

#### 12. **Deployment và DevOps**

- [ ] Dockerfile
- [ ] Docker Compose với MySQL
- [ ] CI/CD pipeline
- [ ] Environment-specific configurations

## Cài đặt và chạy dự án

### Prerequisites

- Java 17
- MySQL 8.0+
- Gradle 7.0+

### Cấu hình Database

```sql
CREATE DATABASE finance_management;
CREATE USER 'finance_user'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON finance_management.* TO 'finance_user'@'localhost';
FLUSH PRIVILEGES;
```

### Cấu hình application.properties

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/finance_management
spring.datasource.username=finance_user
spring.datasource.password=password123

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# JWT
app.jwt.secret=mySecretKey
app.jwt.expiration=86400000

# OAuth2 (Google)
spring.security.oauth2.client.registration.google.client-id=your-client-id
spring.security.oauth2.client.registration.google.client-secret=your-client-secret
```

### Chạy ứng dụng

```bash
./gradlew bootRun
```

## API Endpoints

### Authentication

| Method | Endpoint                           | Description         | Body                  |
| ------ | ---------------------------------- | ------------------- | --------------------- |
| POST   | `/api/auth/register`               | Đăng ký tài khoản   | RegisterRequest       |
| POST   | `/api/auth/login`                  | Đăng nhập           | LoginRequest          |
| GET    | `/api/auth/activate?token={token}` | Kích hoạt tài khoản | -                     |
| POST   | `/api/auth/forgot-password`        | Quên mật khẩu       | ForgotPasswordRequest |
| POST   | `/api/auth/reset-password`         | Đặt lại mật khẩu    | ResetPasswordRequest  |

### User Management

| Method | Endpoint                    | Description           | Body                  | Auth Required |
| ------ | --------------------------- | --------------------- | --------------------- | ------------- |
| GET    | `/api/user/profile`         | Lấy thông tin profile | -                     | ✅            |
| PUT    | `/api/user/profile`         | Cập nhật profile      | UpdateProfileRequest  | ✅            |
| POST   | `/api/user/change-password` | Đổi mật khẩu          | ChangePasswordRequest | ✅            |
| DELETE | `/api/user/account`         | Xóa tài khoản         | -                     | ✅            |

## Ví dụ Request/Response

### Đăng ký

```json
POST /api/auth/register
{
    "email": "user@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "0123456789"
}
```

### Đăng nhập

```json
POST /api/auth/login
{
    "email": "user@example.com",
    "password": "password123"
}

Response:
{
    "success": true,
    "message": "Đăng nhập thành công!",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "type": "Bearer",
        "user": {
            "id": 1,
            "email": "user@example.com",
            "firstName": "John",
            "lastName": "Doe",
            "status": "ACTIVE"
        }
    }
}
```

## Hướng dẫn triển khai từng bước

### Phase 1: Hoàn thiện cơ bản (Ưu tiên cao)

#### 1.1. Tạo SecurityConfig

```java
// Tạo file: security/SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // Cấu hình security filter chain
}
```

#### 1.2. Cấu hình application.properties

```properties
# Cần bổ sung đầy đủ cấu hình cho:
- Database connection
- JWT settings
- Email SMTP
- OAuth2 credentials
```

#### 1.3. Tạo Global Exception Handler

```java
// Tạo file: exception/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Xử lý tất cả exceptions
}
```

### Phase 2: OAuth2 Social Login (Ưu tiên trung bình)

#### 2.1. Google OAuth2

```java
// Files cần tạo:
- OAuth2LoginSuccessHandler.java
- OAuth2UserInfo.java
- GoogleOAuth2UserInfo.java
```

#### 2.2. Facebook & GitHub OAuth2

```java
// Mở rộng support cho:
- FacebookOAuth2UserInfo.java
- GitHubOAuth2UserInfo.java
```

### Phase 3: API Logout & JWT Management

#### 3.1. Logout Endpoint

```java
// Thêm vào AuthController:
@PostMapping("/logout")
public ResponseEntity<ApiResponse> logout() {
    // Implement JWT blacklist
}
```

#### 3.2. JWT Blacklist

```java
// Tạo service để quản lý JWT blacklist
- JwtBlacklistService.java
- Redis hoặc in-memory cache
```

### Phase 4: Testing & Documentation

#### 4.1. Unit Tests

```java
// Tạo tests cho:
- UserServiceTest.java
- AuthControllerTest.java
- SecurityConfigTest.java
```

#### 4.2. API Documentation

```java
// Thêm Swagger:
- SwaggerConfig.java
- @ApiOperation annotations
```

### Phase 5: Production Ready

#### 5.1. Docker & Deployment

```dockerfile
# Dockerfile
# docker-compose.yml với MySQL
```

#### 5.2. Monitoring

```java
// Spring Boot Actuator
// Metrics và health checks
```

## Checklist hoàn thành

### ✅ Đã hoàn thành

- [x] User Entity và Repository
- [x] Authentication APIs (register, login, activate, forgot/reset password)
- [x] User Management APIs (profile, change password, delete account)
- [x] JWT Utility
- [x] Email Service
- [x] DTOs và Mappers
- [x] Basic Security Components

### 🔲 Đang thiếu (Cần làm ngay)

- [ ] SecurityConfig.java hoàn chỉnh
- [ ] GlobalExceptionHandler.java
- [ ] Application.properties đầy đủ
- [ ] Logout API
- [ ] OAuth2 Social Login
- [ ] Unit Tests
- [ ] API Documentation

### 🔲 Tính năng mở rộng (Có thể làm sau)

- [ ] Role-based Authorization
- [ ] Audit Logging
- [ ] Rate Limiting
- [ ] Caching với Redis
- [ ] Docker deployment
- [ ] CI/CD pipeline

## Lỗi thường gặp và cách khắc phục

### 1. JWT Dependencies không tìm thấy

```bash
# Chạy để tải dependencies:
./gradlew build --refresh-dependencies
```

### 2. Database connection failed

```properties
# Kiểm tra cấu hình database trong application.properties
# Đảm bảo MySQL đang chạy và database đã được tạo
```

### 3. Email service không hoạt động

```properties
# Cần cấu hình App Password cho Gmail
# Không dùng password thường
```

## Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

```json
POST /api/auth/login
{
    "email": "user@example.com",
    "password": "password123"
}

Response:
{
    "success": true,
    "message": "Đăng nhập thành công!",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "type": "Bearer",
        "user": {
            "id": 1,
            "email": "user@example.com",
            "firstName": "John",
            "lastName": "Doe",
            "status": "ACTIVE"
        }
    }
}
```

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/finance_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password

# Email
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 4. Build và chạy ứng dụng

```bash
./gradlew build
./gradlew bootRun
```

Ứng dụng sẽ chạy trên `http://localhost:8080`

## API Endpoints

### Authentication APIs

#### 1. Đăng ký

```
POST /api/auth/register
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "0123456789"
}
```

#### 2. Đăng nhập

```
POST /api/auth/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password123"
}
```

#### 3. Kích hoạt tài khoản

```
GET /api/auth/activate?token=<activation_token>
```

#### 4. Quên mật khẩu

```
POST /api/auth/forgot-password
Content-Type: application/json

{
    "email": "user@example.com"
}
```

#### 5. Đặt lại mật khẩu

```
POST /api/auth/reset-password
Content-Type: application/json

{
    "token": "<reset_token>",
    "newPassword": "newpassword123"
}
```

### User APIs (Cần JWT Token)

#### 1. Lấy thông tin profile

```
GET /api/user/profile
Authorization: Bearer <jwt_token>
```

#### 2. Cập nhật profile

```
PUT /api/user/profile
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Smith",
    "phoneNumber": "0987654321"
}
```

#### 3. Đổi mật khẩu

```
POST /api/user/change-password
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
    "currentPassword": "oldpassword",
    "newPassword": "newpassword123"
}
```

#### 4. Xóa tài khoản

```
DELETE /api/user/account
Authorization: Bearer <jwt_token>
```

## Cấu trúc project

```
src/main/java/com/example/backend/
├── config/              # Cấu hình Spring Security
├── controller/          # REST Controllers
├── dto/                 # Data Transfer Objects
│   ├── request/         # Request DTOs
│   └── response/        # Response DTOs
├── entity/              # JPA Entities
├── enums/               # Enums
├── exception/           # Exception handlers
├── mapper/              # Entity-DTO mappers
├── repository/          # JPA Repositories
├── security/            # Security components
├── service/             # Business logic
└── util/                # Utility classes
```

## Cơ sở dữ liệu

### Bảng users

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone_number VARCHAR(20),
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED', 'DELETED') DEFAULT 'INACTIVE',
    auth_provider ENUM('LOCAL', 'GOOGLE', 'FACEBOOK', 'GITHUB') DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    activation_token VARCHAR(255),
    reset_password_token VARCHAR(255),
    reset_password_expires DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## Bảo mật

- Sử dụng JWT tokens cho authentication
- Mật khẩu được mã hóa bằng BCrypt
- Validation dữ liệu đầu vào
- CORS configuration
- Exception handling toàn cục

## Phát triển thêm

Để mở rộng hệ thống, bạn có thể:

1. Thêm OAuth2 social login
2. Implement refresh token
3. Thêm role-based authorization
4. Tích hợp email templates
5. Thêm audit logging
6. Implement rate limiting
7. Thêm API documentation với Swagger

## Testing

Chạy tests:

```bash
./gradlew test
```

## Production Deployment

1. Cấu hình profile production
2. Sử dụng environment variables cho sensitive data
3. Setup proper logging
4. Configure reverse proxy (Nginx)
5. Setup SSL certificates
6. Database backup strategy
