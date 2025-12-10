# 💰 Finance Management Backend

Backend API cho ứng dụng quản lý tài chính cá nhân được xây dựng với Spring Boot, cung cấp đầy đủ tính năng xác thực, quản lý người dùng, và báo cáo tài chính. 

## 📋 Mục lục

- [Tính năng](#-tính-năng)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Chạy ứng dụng](#-chạy-ứng-dụng)
- [API Documentation](#-api-documentation)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Troubleshooting](#-troubleshooting)

## ✨ Tính năng

### 🔐 Xác thực & Bảo mật
-  Đăng ký/Đăng nhập với JWT Authentication
-  OAuth2 Google Login
-  Email verification
-  Forgot/Reset password
-  Spring Security với custom filters

### 👤 Quản lý người dùng
-  Thông tin profile
-  Upload avatar
-  Cập nhật thông tin cá nhân
-  Quản lý sessions

### 📊 Tính năng tài chính
-  Quản lý thu chi
-  Phân loại giao dịch
-  Thống kê & báo cáo
-  Xuất báo cáo Excel
-  Xuất báo cáo PDF
-  Gửi báo cáo qua email tự động

### 📧 Email Service
-  Gửi email xác thực
-  Gửi email reset password
-  Gửi báo cáo định kỳ
-  Template email tùy chỉnh

## 🛠 Công nghệ sử dụng

### Core Framework
- **Spring Boot 3.5.5** - Framework chính
- **Java 17** - Ngôn ngữ lập trình
- **Gradle** - Build tool

### Database & ORM
- **MySQL 8.0** - Database
- **Spring Data JPA** - ORM
- **Hibernate** - JPA implementation

### Security
- **Spring Security** - Security framework
- **JWT (jjwt 0.12.3)** - Token authentication
- **OAuth2 Client** - Social login

### Utilities
- **Lombok** - Reduce boilerplate code
- **MapStruct 1.5.5** - DTO mapping
- **Apache POI 5.2.4** - Excel export
- **iText7 7.2.5** - PDF export
- **Spring Mail** - Email service
- **WebFlux** - Reactive HTTP client

##  Yêu cầu hệ thống

- **Java**:  17 hoặc cao hơn
- **MySQL**: 8.0 hoặc cao hơn
- **Gradle**: 7.x+ (hoặc dùng Gradle Wrapper có sẵn)
- **RAM**:  Tối thiểu 2GB
- **Disk Space**: 500MB+

##  Cài đặt

### 1. Clone Repository

```bash
git clone https://github.com/Yenhi6886/finance-management-be. git
cd finance-management-be
```

### 2. Cài đặt MySQL

#### MacOS (Homebrew)
```bash
brew install mysql
brew services start mysql
```

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
```

#### Windows
Tải và cài đặt từ [MySQL Official Website](https://dev.mysql.com/downloads/mysql/)

### 3. Tạo Database

```sql
mysql -u root -p

CREATE DATABASE finance_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'finance_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON finance_management.* TO 'finance_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 4. Cài đặt Java 17

#### MacOS
```bash
brew install openjdk@17
```

#### Ubuntu/Debian
```bash
sudo apt install openjdk-17-jdk
```

#### Windows
Tải từ [Adoptium](https://adoptium.net/)

## ⚙️ Cấu hình

### Cấu hình biến môi trường

Tạo file `.env` ở thư mục gốc hoặc set biến môi trường: 

```properties
# ==========================================
# APPLICATION
# ==========================================
SPRING_APPLICATION_NAME=finance-management-backend

# ==========================================
# DATABASE CONFIGURATION
# ==========================================
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/finance_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=finance_user
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=com. mysql.cj.jdbc.Driver

# ==========================================
# JPA/HIBERNATE CONFIGURATION
# ==========================================
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect
SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true

# ==========================================
# JWT CONFIGURATION
# ==========================================
# Generate a secure key:  openssl rand -base64 64
APP_JWT_SECRET=your-256-bit-secret-key-here-make-it-long-and-secure
APP_JWT_EXPIRATION=86400000

# ==========================================
# EMAIL CONFIGURATION
# ==========================================
APP_MAIL_ENABLED=true
APP_MAIL_FROM=noreply@yourapp.com
SPRING_MAIL_HOST=smtp.gmail. com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-specific-password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

# ==========================================
# FRONTEND CONFIGURATION
# ==========================================
APP_FRONTEND_URL=http://localhost:3000

# ==========================================
# FILE UPLOAD
# ==========================================
FILE_UPLOAD_DIR=./uploads

# ==========================================
# LOGGING
# ==========================================
LOGGING_LEVEL_COM_EXAMPLE_BACKEND=INFO
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=WARN

# ==========================================
# OAUTH2 - GOOGLE
# ==========================================
GOOGLE_CLIENT_ID=your-google-client-id. apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret

# ==========================================
# REPORT EMAIL SCHEDULING
# ==========================================
APP_REPORT_EMAIL_ENABLED=false
APP_REPORT_EMAIL_CRON=0 0 8 * * *
```

###  Cấu hình Gmail cho Email Service

1. **Bật 2-Step Verification**
   - Truy cập:  https://myaccount.google.com/security
   - Bật "2-Step Verification"

2. **Tạo App Password**
   - Truy cập: https://myaccount.google.com/apppasswords
   - Chọn app:  "Mail"
   - Chọn device: "Other" → Nhập tên
   - Copy password 16 ký tự
   - Dùng password này cho `SPRING_MAIL_PASSWORD`

###  Cấu hình Google OAuth2

1. **Truy cập Google Cloud Console**
   - https://console.cloud.google.com/

2. **Tạo Project mới**
   - Click "Select a project" → "New Project"
   - Nhập tên project → Create

3. **Enable Google+ API**
   - APIs & Services → Library
   - Tìm "Google+ API" → Enable

4. **Tạo OAuth 2.0 Credentials**
   - APIs & Services → Credentials
   - Create Credentials → OAuth Client ID
   - Application type: Web application
   - Authorized redirect URIs: 
     ```
     http://localhost:8080/api/auth/oauth2/callback/google
     http://localhost:8080/login/oauth2/code/google
     ```
   - Copy Client ID và Client Secret

5. **Cấu hình OAuth Consent Screen**
   - User Type: External
   - Thêm email và thông tin cần thiết
   - Add scopes: email, profile, openid

##  Chạy ứng dụng

### Development Mode

#### Linux/MacOS
```bash
# Build project
./gradlew clean build

# Run application
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### Windows
```bash
gradlew.bat clean build
gradlew.bat bootRun
```

### Production Mode

```bash
# Build JAR file
./gradlew clean build -x test

# Run JAR
java -jar build/libs/backend-0.0.1-SNAPSHOT. jar

# Run with environment variables
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar \
  --SPRING_DATASOURCE_URL=jdbc:mysql://production-host:3306/db \
  --SPRING_DATASOURCE_USERNAME=user \
  --SPRING_DATASOURCE_PASSWORD=pass
```

### Docker (Optional)

```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY build/libs/*. jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app. jar"]
```

```bash
# Build Docker image
docker build -t finance-management-be .

# Run container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/finance_management \
  finance-management-be
```

##  API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user123",
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user123",
  "password": "SecurePass123!"
}

Response:
{
  "token":  "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

#### Google OAuth2
```http
GET /api/auth/oauth2/google
→ Redirects to Google Login
```

#### Forgot Password
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "reset-token-from-email",
  "newPassword": "NewSecurePass123!"
}
```

### Protected Endpoints

Tất cả endpoints bên dưới yêu cầu JWT token trong header: 
```http
Authorization: Bearer <your-jwt-token>
```

#### Get User Profile
```http
GET /api/users/me
```

#### Update Profile
```http
PUT /api/users/me
Content-Type:  application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

#### Upload Avatar
```http
POST /api/users/me/avatar
Content-Type: multipart/form-data

file: <image-file>
```

### Report Endpoints

#### Export to Excel
```http
GET /api/reports/export/excel? startDate=2024-01-01&endDate=2024-12-31
```

#### Export to PDF
```http
GET /api/reports/export/pdf?startDate=2024-01-01&endDate=2024-12-31
```

#### Send Report via Email
```http
POST /api/reports/email
Content-Type: application/json

{
  "email": "recipient@example.com",
  "reportType": "MONTHLY",
  "month": "2024-12"
}
```
```

##  Testing

### Chạy tất cả tests
```bash
./gradlew test
```

### Chạy tests với coverage
```bash
./gradlew test jacocoTestReport
```

### Chạy specific test class
```bash
./gradlew test --tests UserServiceTest
```

##  Troubleshooting

###  Lỗi: "Access denied for user"
**Nguyên nhân**: Sai username/password MySQL hoặc chưa grant quyền

**Giải pháp**:
```sql
GRANT ALL PRIVILEGES ON finance_management.* TO 'finance_user'@'localhost';
FLUSH PRIVILEGES;
```

###  Lỗi:  "JWT signature does not match"
**Nguyên nhân**: JWT secret key không đúng hoặc quá ngắn

**Giải pháp**:
```bash
# Generate secure key
openssl rand -base64 64

# Update APP_JWT_SECRET với key mới
```

###  Lỗi: "Failed to send email"
**Nguyên nhân**: Sai App Password hoặc chưa bật 2FA

**Giải pháp**:
1. Kiểm tra 2-Step Verification đã bật
2. Tạo lại App Password
3. Kiểm tra SMTP settings

###  Lỗi: "Port 8080 already in use"
**Giải pháp**:
```bash
# Tìm process đang dùng port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Hoặc đổi port trong application.properties
server.port=8081
```

###  Lỗi: "Table doesn't exist"
**Nguyên nhân**: Database chưa được tạo hoặc Hibernate không tự động tạo table

**Giải pháp**: 
```properties
# Set ddl-auto to create (chỉ lần đầu)
SPRING_JPA_HIBERNATE_DDL_AUTO=create

# Sau đó đổi lại thành update
SPRING_JPA_HIBERNATE_DDL_AUTO=update

---

⭐️ Nếu project này hữu ích, hãy cho một star nhé! 
