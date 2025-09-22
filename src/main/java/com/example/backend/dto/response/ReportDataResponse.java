package com.example.backend.dto.response;

import com.example.backend.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReportDataResponse {
    
    private String reportTitle;
    private LocalDateTime generatedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String userName;
    private String userEmail;
    
    // Tổng quan
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netAmount;
    private int totalTransactions;
    
    // Chi tiết giao dịch
    private List<TransactionReportData> transactions;
    
    // Thống kê theo danh mục
    private List<CategoryReportData> categoryStats;
    
    // Thống kê theo ví
    private List<WalletReportData> walletStats;
    
    @Data
    @Builder
    public static class TransactionReportData {
        private Long id;
        private BigDecimal amount;
        private TransactionType type;
        private String description;
        private Instant date;
        private String categoryName;
        private String walletName;
        private BigDecimal balanceAfterTransaction;
    }
    
    @Data
    @Builder
    public static class CategoryReportData {
        private Long categoryId;
        private String categoryName;
        private BigDecimal totalAmount;
        private int transactionCount;
        private TransactionType type;
        private BigDecimal percentage;
    }
    
    @Data
    @Builder
    public static class WalletReportData {
        private Long walletId;
        private String walletName;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal netAmount;
        private int transactionCount;
        private BigDecimal currentBalance;
    }
}
