package com.example.backend.controller;

import com.example.backend.dto.request.ReportRequest;
import com.example.backend.dto.response.ReportDataResponse;
import com.example.backend.service.ExcelService;
import com.example.backend.service.PDFService;
import com.example.backend.service.ReportService;
import com.example.backend.service.EmailService;
import com.example.backend.service.ReportEmailSettingService;
import com.example.backend.entity.ReportEmailSetting;
import com.example.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final ExcelService excelService;
    private final PDFService pdfService;
    private final EmailService emailService;
    private final ReportEmailSettingService settingService;

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Report API is working!");
    }

    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcelReport(
            @RequestBody ReportRequest request,
            Authentication authentication) {
        try {
            // Validation
            if (request == null) {
                return ResponseEntity.badRequest().body("Request body không được để trống".getBytes());
            }
            
            if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseEntity.badRequest().body("Ngày bắt đầu và ngày kết thúc không được để trống".getBytes());
            }

            if (request.getStartDate().isAfter(request.getEndDate())) {
                return ResponseEntity.badRequest().body("Ngày bắt đầu không được sau ngày kết thúc".getBytes());
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            ReportDataResponse reportData = reportService.generateReportData(request, userId);

            byte[] excelBytes = excelService.generateExcelReport(reportData);

            String fileName = generateFileName("BaoCaoTaiChinh", "xlsx");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(excelBytes.length);

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Lỗi tạo file Excel: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Lỗi xử lý request: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPDFReport(
            @RequestBody ReportRequest request,
            Authentication authentication) {
        try {
            if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseEntity.badRequest().body("Ngày bắt đầu và ngày kết thúc không được để trống".getBytes());
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            ReportDataResponse reportData = reportService.generateReportData(request, userId);

            byte[] pdfBytes = pdfService.generatePDFReport(reportData);

            String fileName = generateFileName("BaoCaoTaiChinh", "pdf");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/email/pdf")
    public ResponseEntity<Void> emailPDFReport(
            @RequestBody ReportRequest request,
            Authentication authentication) {
        try {
            if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseEntity.badRequest().build();
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            ReportDataResponse reportData = reportService.generateReportData(request, userId);
            byte[] pdfBytes = pdfService.generatePDFReport(reportData);

            String fileName = generateFileName("BaoCaoTaiChinh", "pdf");
            String subject = "Báo cáo tài chính của bạn";
            String body = "Đính kèm là báo cáo tài chính theo khoảng thời gian bạn đã chọn.";

            emailService.sendEmailWithAttachment(userDetails.getUsername(), subject, body, pdfBytes, fileName, "application/pdf");

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<ReportDataResponse> previewReport(
            @RequestBody ReportRequest request,
            Authentication authentication) {
        try {
            // Validation thủ công
            if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();
            
            // Tạo dữ liệu báo cáo để preview
            ReportDataResponse reportData = reportService.generateReportData(request, userId);
            
            return ResponseEntity.ok(reportData);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ========== Email report settings ==========
    @GetMapping("/email/settings")
    public ResponseEntity<ReportEmailSetting> getEmailSettings(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        ReportEmailSetting setting = settingService.getByUserId(userId).orElse(null);
        return ResponseEntity.ok(setting);
    }

    @PostMapping("/email/settings")
    public ResponseEntity<ReportEmailSetting> saveEmailSettings(
            @RequestBody ReportEmailSetting payload,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();
        ReportEmailSetting saved = settingService.upsert(userId, payload);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/email/send-now")
    public ResponseEntity<Void> sendNow(
            @RequestBody ReportRequest request,
            Authentication authentication) {
        try {
            if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseEntity.badRequest().build();
            }
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getId();

            ReportDataResponse reportData = reportService.generateReportData(request, userId);
            byte[] pdfBytes = pdfService.generatePDFReport(reportData);

            String fileName = generateFileName("BaoCaoTaiChinh", "pdf");
            String subject = "Báo cáo tài chính của bạn";
            String body = "Đính kèm là báo cáo tài chính theo khoảng thời gian bạn đã chọn.";

            String toEmail = settingService.getByUserId(userId)
                    .map(ReportEmailSetting::getTargetEmail)
                    .filter(e -> e != null && !e.isBlank())
                    .orElse(userDetails.getUsername());

            emailService.sendEmailWithAttachment(toEmail, subject, body, pdfBytes, fileName, "application/pdf");
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private String generateFileName(String prefix, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return String.format("%s_%s.%s", prefix, timestamp, extension);
    }
}
