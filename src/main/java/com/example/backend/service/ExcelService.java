package com.example.backend.service;

import com.example.backend.dto.response.ReportDataResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateExcelReport(ReportDataResponse reportData) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Tạo sheet tổng quan
            createSummarySheet(workbook, reportData);
            
            // Tạo sheet chi tiết giao dịch
            createTransactionDetailSheet(workbook, reportData);
            
            // Tạo sheet thống kê theo danh mục
            createCategoryStatsSheet(workbook, reportData);
            
            // Tạo sheet thống kê theo ví
            createWalletStatsSheet(workbook, reportData);

            // Ghi workbook vào ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createSummarySheet(Workbook workbook, ReportDataResponse reportData) {
        Sheet sheet = workbook.createSheet("Tổng Quan");
        
        // Tạo style cho header
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 0;
        
        // Header thông tin báo cáo
        Row headerRow = sheet.createRow(rowNum++);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("BÁO CÁO TÀI CHÍNH");
        headerCell.setCellStyle(headerStyle);
        
        // Thông tin người dùng
        createInfoRow(sheet, rowNum++, "Người dùng:", reportData.getUserName(), dataStyle);
        createInfoRow(sheet, rowNum++, "Email:", reportData.getUserEmail(), dataStyle);
        createInfoRow(sheet, rowNum++, "Từ ngày:", reportData.getStartDate().format(DATE_ONLY_FORMATTER), dataStyle);
        createInfoRow(sheet, rowNum++, "Đến ngày:", reportData.getEndDate().format(DATE_ONLY_FORMATTER), dataStyle);
        createInfoRow(sheet, rowNum++, "Ngày tạo:", reportData.getGeneratedAt().format(DATE_FORMATTER), dataStyle);
        
        rowNum++; // Dòng trống
        
        // Tổng quan tài chính
        Row summaryHeaderRow = sheet.createRow(rowNum++);
        Cell summaryHeaderCell = summaryHeaderRow.createCell(0);
        summaryHeaderCell.setCellValue("TỔNG QUAN TÀI CHÍNH");
        summaryHeaderCell.setCellStyle(headerStyle);
        
        createInfoRow(sheet, rowNum++, "Tổng thu nhập:", formatCurrency(reportData.getTotalIncome()), dataStyle);
        createInfoRow(sheet, rowNum++, "Tổng chi tiêu:", formatCurrency(reportData.getTotalExpense()), dataStyle);
        createInfoRow(sheet, rowNum++, "Số dư ròng:", formatCurrency(reportData.getNetAmount()), dataStyle);
        createInfoRow(sheet, rowNum++, "Tổng số giao dịch:", String.valueOf(reportData.getTotalTransactions()), dataStyle);
        
        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createTransactionDetailSheet(Workbook workbook, ReportDataResponse reportData) {
        Sheet sheet = workbook.createSheet("Chi Tiết Giao Dịch");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 0;
        
        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID", "Ngày", "Loại", "Số tiền", "Mô tả", "Danh mục", "Ví", "Số dư sau GD"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        for (ReportDataResponse.TransactionReportData transaction : reportData.getTransactions()) {
            Row dataRow = sheet.createRow(rowNum++);
            
            dataRow.createCell(0).setCellValue(transaction.getId());
            dataRow.createCell(1).setCellValue(convertInstantToLocalDateTime(transaction.getDate()).format(DATE_FORMATTER));
            dataRow.createCell(2).setCellValue(transaction.getType().toString());
            dataRow.createCell(3).setCellValue(transaction.getAmount().doubleValue());
            dataRow.createCell(4).setCellValue(transaction.getDescription());
            dataRow.createCell(5).setCellValue(transaction.getCategoryName());
            dataRow.createCell(6).setCellValue(transaction.getWalletName());
            dataRow.createCell(7).setCellValue(transaction.getBalanceAfterTransaction().doubleValue());
            
            // Apply data style to all cells
            for (int i = 0; i < 8; i++) {
                dataRow.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createCategoryStatsSheet(Workbook workbook, ReportDataResponse reportData) {
        Sheet sheet = workbook.createSheet("Thống Kê Theo Danh Mục");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 0;
        
        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Danh mục", "Loại", "Tổng số tiền", "Số giao dịch", "Tỷ lệ %"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        for (ReportDataResponse.CategoryReportData category : reportData.getCategoryStats()) {
            Row dataRow = sheet.createRow(rowNum++);
            
            dataRow.createCell(0).setCellValue(category.getCategoryName());
            dataRow.createCell(1).setCellValue(category.getType().toString());
            dataRow.createCell(2).setCellValue(category.getTotalAmount().doubleValue());
            dataRow.createCell(3).setCellValue(category.getTransactionCount());
            dataRow.createCell(4).setCellValue(category.getPercentage().doubleValue());
            
            // Apply data style to all cells
            for (int i = 0; i < 5; i++) {
                dataRow.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createWalletStatsSheet(Workbook workbook, ReportDataResponse reportData) {
        Sheet sheet = workbook.createSheet("Thống Kê Theo Ví");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        int rowNum = 0;
        
        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Tên ví", "Thu nhập", "Chi tiêu", "Số dư ròng", "Số giao dịch", "Số dư hiện tại"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        for (ReportDataResponse.WalletReportData wallet : reportData.getWalletStats()) {
            Row dataRow = sheet.createRow(rowNum++);
            
            dataRow.createCell(0).setCellValue(wallet.getWalletName());
            dataRow.createCell(1).setCellValue(wallet.getTotalIncome().doubleValue());
            dataRow.createCell(2).setCellValue(wallet.getTotalExpense().doubleValue());
            dataRow.createCell(3).setCellValue(wallet.getNetAmount().doubleValue());
            dataRow.createCell(4).setCellValue(wallet.getTransactionCount());
            dataRow.createCell(5).setCellValue(wallet.getCurrentBalance().doubleValue());
            
            // Apply data style to all cells
            for (int i = 0; i < 6; i++) {
                dataRow.getCell(i).setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createInfoRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        return String.format("%,.0f VND", amount.doubleValue());
    }

    private LocalDateTime convertInstantToLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
