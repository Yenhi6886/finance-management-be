package com.example.backend.service;

import com.example.backend.dto.response.ReportDataResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        int rowNum = 0;
        
        // Tiêu đề báo cáo
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(reportData.getReportTitle());
        titleCell.setCellStyle(createTitleStyle(workbook));
        
        // Thông tin báo cáo
        rowNum++;
        createInfoRow(sheet, rowNum++, "Người dùng:", reportData.getUserName(), dataStyle);
        createInfoRow(sheet, rowNum++, "Email:", reportData.getUserEmail(), dataStyle);
        createInfoRow(sheet, rowNum++, "Từ ngày:", reportData.getStartDate().format(DATE_ONLY_FORMATTER), dataStyle);
        createInfoRow(sheet, rowNum++, "Đến ngày:", reportData.getEndDate().format(DATE_ONLY_FORMATTER), dataStyle);
        createInfoRow(sheet, rowNum++, "Ngày tạo báo cáo:", reportData.getGeneratedAt().format(DATE_FORMATTER), dataStyle);
        
        rowNum++;
        
        // Tổng quan tài chính
        Row summaryHeaderRow = sheet.createRow(rowNum++);
        Cell summaryHeaderCell = summaryHeaderRow.createCell(0);
        summaryHeaderCell.setCellValue("TỔNG QUAN TÀI CHÍNH");
        summaryHeaderCell.setCellStyle(headerStyle);
        
        createSummaryRow(sheet, rowNum++, "Tổng thu nhập:", reportData.getTotalIncome(), currencyStyle);
        createSummaryRow(sheet, rowNum++, "Tổng chi tiêu:", reportData.getTotalExpense(), currencyStyle);
        createSummaryRow(sheet, rowNum++, "Số dư ròng:", reportData.getNetAmount(), currencyStyle);
        createSummaryRow(sheet, rowNum++, "Tổng số giao dịch:", reportData.getTotalTransactions(), dataStyle);
        
        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createTransactionDetailSheet(Workbook workbook, ReportDataResponse reportData) {
        Sheet sheet = workbook.createSheet("Chi Tiết Giao Dịch");
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // Tạo header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"STT", "Ngày", "Loại", "Số tiền", "Danh mục", "Ví", "Mô tả", "Số dư sau GD"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Tạo dữ liệu
        List<ReportDataResponse.TransactionReportData> transactions = reportData.getTransactions();
        for (int i = 0; i < transactions.size(); i++) {
            Row row = sheet.createRow(i + 1);
            ReportDataResponse.TransactionReportData transaction = transactions.get(i);
            
            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(transaction.getDate().format(DATE_FORMATTER));
            row.createCell(2).setCellValue(transaction.getType().toString());
            
            Cell amountCell = row.createCell(3);
            amountCell.setCellValue(transaction.getAmount().doubleValue());
            amountCell.setCellStyle(currencyStyle);
            
            row.createCell(4).setCellValue(transaction.getCategoryName());
            row.createCell(5).setCellValue(transaction.getWalletName());
            row.createCell(6).setCellValue(transaction.getDescription() != null ? transaction.getDescription() : "");
            
            Cell balanceCell = row.createCell(7);
            balanceCell.setCellValue(transaction.getBalanceAfterTransaction().doubleValue());
            balanceCell.setCellStyle(currencyStyle);
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
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // Tạo header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Danh mục", "Loại", "Số tiền", "Số GD", "Tỷ lệ %"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Tạo dữ liệu
        List<ReportDataResponse.CategoryReportData> categoryStats = reportData.getCategoryStats();
        for (int i = 0; i < categoryStats.size(); i++) {
            Row row = sheet.createRow(i + 1);
            ReportDataResponse.CategoryReportData category = categoryStats.get(i);
            
            row.createCell(0).setCellValue(category.getCategoryName());
            row.createCell(1).setCellValue(category.getType().toString());
            
            Cell amountCell = row.createCell(2);
            amountCell.setCellValue(category.getTotalAmount().doubleValue());
            amountCell.setCellStyle(currencyStyle);
            
            row.createCell(3).setCellValue(category.getTransactionCount());
            
            Cell percentageCell = row.createCell(4);
            percentageCell.setCellValue(category.getPercentage().doubleValue());
            percentageCell.setCellStyle(dataStyle);
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
        CellStyle currencyStyle = createCurrencyStyle(workbook);

        // Tạo header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Ví", "Thu nhập", "Chi tiêu", "Số dư ròng", "Số GD", "Số dư hiện tại"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Tạo dữ liệu
        List<ReportDataResponse.WalletReportData> walletStats = reportData.getWalletStats();
        for (int i = 0; i < walletStats.size(); i++) {
            Row row = sheet.createRow(i + 1);
            ReportDataResponse.WalletReportData wallet = walletStats.get(i);
            
            row.createCell(0).setCellValue(wallet.getWalletName());
            
            Cell incomeCell = row.createCell(1);
            incomeCell.setCellValue(wallet.getTotalIncome().doubleValue());
            incomeCell.setCellStyle(currencyStyle);
            
            Cell expenseCell = row.createCell(2);
            expenseCell.setCellValue(wallet.getTotalExpense().doubleValue());
            expenseCell.setCellStyle(currencyStyle);
            
            Cell netCell = row.createCell(3);
            netCell.setCellValue(wallet.getNetAmount().doubleValue());
            netCell.setCellStyle(currencyStyle);
            
            row.createCell(4).setCellValue(wallet.getTransactionCount());
            
            Cell balanceCell = row.createCell(5);
            balanceCell.setCellValue(wallet.getCurrentBalance().doubleValue());
            balanceCell.setCellStyle(currencyStyle);
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

    private void createSummaryRow(Sheet sheet, int rowNum, String label, Object value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        
        Cell valueCell = row.createCell(1);
        if (value instanceof Number) {
            valueCell.setCellValue(((Number) value).doubleValue());
        } else {
            valueCell.setCellValue(value.toString());
        }
        valueCell.setCellStyle(style);
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
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

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("#,##0.00"));
        return style;
    }
}
