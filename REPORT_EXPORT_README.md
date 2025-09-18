# Báo Cáo Tài Chính - Tính Năng Xuất Excel/PDF

## Tổng Quan

Tính năng xuất báo cáo tài chính cho phép người dùng tạo và tải xuống báo cáo chi tiết về tình hình tài chính của họ theo định dạng Excel (.xlsx) hoặc PDF.

## Các API Endpoints

### 1. Xuất Báo Cáo Excel
```
POST /api/reports/export/excel
```

### 2. Xuất Báo Cáo PDF
```
POST /api/reports/export/pdf
```

### 3. Xem Trước Báo Cáo (Preview)
```
POST /api/reports/preview
```

## Request Body

```json
{
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-12-31T23:59:59",
  "walletIds": [1, 2, 3],
  "transactionTypes": ["INCOME", "EXPENSE"],
  "categoryIds": [1, 2, 3],
  "reportType": "EXCEL",
  "reportFormat": "DETAILED"
}
```

### Các Trường Dữ Liệu

- **startDate** (bắt buộc): Ngày bắt đầu
- **endDate** (bắt buộc): Ngày kết thúc
- **walletIds** (tùy chọn): Danh sách ID ví để lọc
- **transactionTypes** (tùy chọn): Loại giao dịch (INCOME, EXPENSE, TRANSFER)
- **categoryIds** (tùy chọn): Danh sách ID danh mục để lọc
- **reportType**: EXCEL hoặc PDF
- **reportFormat**: DETAILED, SUMMARY, hoặc CATEGORY

## Nội Dung Báo Cáo

### Báo Cáo Excel
Bao gồm 4 sheet:
1. **Tổng Quan**: Thông tin tổng quan về tài chính
2. **Chi Tiết Giao Dịch**: Danh sách tất cả giao dịch
3. **Thống Kê Theo Danh Mục**: Phân tích theo từng danh mục
4. **Thống Kê Theo Ví**: Phân tích theo từng ví

### Báo Cáo PDF
Bao gồm các phần:
1. **Header**: Thông tin người dùng và khoảng thời gian
2. **Tổng Quan Tài Chính**: Tổng thu nhập, chi tiêu, số dư ròng
3. **Thống Kê Theo Danh Mục**: Bảng thống kê chi tiết
4. **Thống Kê Theo Ví**: Bảng thống kê theo ví
5. **Chi Tiết Giao Dịch**: 50 giao dịch gần nhất

## Các Dependencies Đã Thêm

### Excel Export
```gradle
implementation 'org.apache.poi:poi:5.2.4'
implementation 'org.apache.poi:poi-ooxml:5.2.4'
implementation 'org.apache.poi:poi-scratchpad:5.2.4'
```

### PDF Export
```gradle
implementation 'com.itextpdf:itext7-core:7.2.5'
implementation 'com.itextpdf:html2pdf:4.0.5'
```

## Các Service Classes

1. **ReportService**: Xử lý logic tạo dữ liệu báo cáo
2. **ExcelService**: Tạo file Excel với Apache POI
3. **PDFService**: Tạo file PDF với iText7
4. **ReportController**: API endpoints

## Cách Sử Dụng

### Từ Frontend
1. Truy cập trang "Báo Cáo Tài Chính"
2. Chọn khoảng thời gian (bắt buộc)
3. Tùy chọn lọc theo ví, loại giao dịch
4. Nhấn "Xuất Excel" hoặc "Xuất PDF"
5. File sẽ được tải xuống tự động

### Từ API
```bash
curl -X POST "http://localhost:8080/api/reports/export/excel" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2024-01-01T00:00:00",
    "endDate": "2024-12-31T23:59:59"
  }'
```

## Lưu Ý

- Người dùng chỉ có thể xuất báo cáo của chính họ
- Báo cáo PDF giới hạn 50 giao dịch gần nhất để tránh file quá lớn
- Báo cáo Excel bao gồm tất cả giao dịch trong khoảng thời gian
- File được tạo với tên có timestamp để tránh trùng lặp

## Xử Lý Lỗi

- **400 Bad Request**: Dữ liệu đầu vào không hợp lệ
- **401 Unauthorized**: Chưa đăng nhập
- **500 Internal Server Error**: Lỗi server khi tạo file

## Bảo Mật

- Tất cả endpoints yêu cầu authentication
- Người dùng chỉ có thể truy cập dữ liệu của chính họ
- File được tạo trong memory và không lưu trữ trên server
