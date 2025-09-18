package com.example.backend.service;

import com.example.backend.dto.response.ReportDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimpleExcelService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateExcelReport(ReportDataResponse reportData) throws IOException {
        // Tạo CSV thay vì Excel để tránh lỗi dependencies
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("BÁO CÁO TÀI CHÍNH\n");
        csv.append("Người dùng: ").append(reportData.getUserName()).append("\n");
        csv.append("Email: ").append(reportData.getUserEmail()).append("\n");
        csv.append("Từ ngày: ").append(reportData.getStartDate().format(DATE_ONLY_FORMATTER)).append("\n");
        csv.append("Đến ngày: ").append(reportData.getEndDate().format(DATE_ONLY_FORMATTER)).append("\n");
        csv.append("Ngày tạo: ").append(reportData.getGeneratedAt().format(DATE_FORMATTER)).append("\n\n");
        
        // Tổng quan
        csv.append("TỔNG QUAN TÀI CHÍNH\n");
        csv.append("Tổng thu nhập,").append(formatCurrency(reportData.getTotalIncome())).append("\n");
        csv.append("Tổng chi tiêu,").append(formatCurrency(reportData.getTotalExpense())).append("\n");
        csv.append("Số dư ròng,").append(formatCurrency(reportData.getNetAmount())).append("\n");
        csv.append("Tổng số giao dịch,").append(reportData.getTotalTransactions()).append("\n\n");
        
        // Chi tiết giao dịch
        csv.append("CHI TIẾT GIAO DỊCH\n");
        csv.append("STT,Ngày,Loại,Số tiền,Danh mục,Ví,Mô tả\n");
        
        List<ReportDataResponse.TransactionReportData> transactions = reportData.getTransactions();
        for (int i = 0; i < transactions.size(); i++) {
            ReportDataResponse.TransactionReportData transaction = transactions.get(i);
            csv.append(i + 1).append(",");
            csv.append(transaction.getDate().format(DATE_FORMATTER)).append(",");
            csv.append(transaction.getType().toString()).append(",");
            csv.append(formatCurrency(transaction.getAmount())).append(",");
            csv.append(transaction.getCategoryName()).append(",");
            csv.append(transaction.getWalletName()).append(",");
            csv.append(transaction.getDescription() != null ? transaction.getDescription() : "").append("\n");
        }
        
        csv.append("\n");
        
        // Thống kê theo danh mục
        csv.append("THỐNG KÊ THEO DANH MỤC\n");
        csv.append("Danh mục,Loại,Số tiền,Số GD,Tỷ lệ %\n");
        
        for (ReportDataResponse.CategoryReportData category : reportData.getCategoryStats()) {
            csv.append(category.getCategoryName()).append(",");
            csv.append(category.getType().toString()).append(",");
            csv.append(formatCurrency(category.getTotalAmount())).append(",");
            csv.append(category.getTransactionCount()).append(",");
            csv.append(String.format("%.2f", category.getPercentage())).append("%\n");
        }
        
        csv.append("\n");
        
        // Thống kê theo ví
        csv.append("THỐNG KÊ THEO VÍ\n");
        csv.append("Ví,Thu nhập,Chi tiêu,Số dư ròng,Số GD,Số dư hiện tại\n");
        
        for (ReportDataResponse.WalletReportData wallet : reportData.getWalletStats()) {
            csv.append(wallet.getWalletName()).append(",");
            csv.append(formatCurrency(wallet.getTotalIncome())).append(",");
            csv.append(formatCurrency(wallet.getTotalExpense())).append(",");
            csv.append(formatCurrency(wallet.getNetAmount())).append(",");
            csv.append(wallet.getTransactionCount()).append(",");
            csv.append(formatCurrency(wallet.getCurrentBalance())).append("\n");
        }
        
        return csv.toString().getBytes("UTF-8");
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        return String.format("%,.0f VNĐ", amount.doubleValue());
    }
}
