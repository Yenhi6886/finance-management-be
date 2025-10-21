# Finance Management Backend

Hệ thống backend quản lý tài chính sử dụng Spring Boot, Java 17, và Gradle.

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
