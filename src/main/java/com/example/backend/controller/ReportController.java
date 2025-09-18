package com.example.backend.controller;

import com.example.backend.dto.request.ReportRequest;
import com.example.backend.dto.response.ReportDataResponse;
import com.example.backend.service.ExcelService;
import com.example.backend.service.PDFService;
import com.example.backend.service.ReportService;
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

    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcelReport(
            @RequestBody ReportRequest request,
            Authentication authentication) {
        try {
            if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseEntity.badRequest().body("Ngày bắt đầu và ngày kết thúc không được để trống".getBytes());
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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

    private String generateFileName(String prefix, String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return String.format("%s_%s.%s", prefix, timestamp, extension);
    }
}
