package com.example.backend.service;

import com.example.backend.dto.request.ReportRequest;
import com.example.backend.dto.response.ReportDataResponse;
import com.example.backend.entity.Category;
import com.example.backend.entity.Transaction;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.enums.TransactionType;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public ReportDataResponse generateReportData(ReportRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userId));

        // Lấy danh sách ví của người dùng
        List<Wallet> userWallets = walletRepository.findByUserId(userId);
        
        // Lọc theo ví nếu có
        List<Long> walletIds = request.getWalletIds() != null && !request.getWalletIds().isEmpty() 
            ? request.getWalletIds() 
            : userWallets.stream().map(Wallet::getId).collect(Collectors.toList());

        // Lấy tất cả giao dịch trong khoảng thời gian
        List<Transaction> transactions = getFilteredTransactions(userId, request, walletIds);

        // Tính toán thống kê
        BigDecimal totalIncome = calculateTotalByType(transactions, TransactionType.INCOME);
        BigDecimal totalExpense = calculateTotalByType(transactions, TransactionType.EXPENSE);
        BigDecimal netAmount = totalIncome.subtract(totalExpense);

        // Tạo dữ liệu chi tiết giao dịch
        List<ReportDataResponse.TransactionReportData> transactionData = transactions.stream()
                .map(this::mapToTransactionReportData)
                .collect(Collectors.toList());

        // Tạo thống kê theo danh mục
        List<ReportDataResponse.CategoryReportData> categoryStats = generateCategoryStats(transactions, totalIncome, totalExpense);

        // Tạo thống kê theo ví
        List<ReportDataResponse.WalletReportData> walletStats = generateWalletStats(transactions, userWallets);

        return ReportDataResponse.builder()
                .reportTitle("Báo Cáo Tài Chính")
                .generatedAt(LocalDateTime.now())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .userName(user.getFirstName() + " " + user.getLastName())
                .userEmail(user.getEmail())
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netAmount(netAmount)
                .totalTransactions(transactions.size())
                .transactions(transactionData)
                .categoryStats(categoryStats)
                .walletStats(walletStats)
                .build();
    }

    private List<Transaction> getFilteredTransactions(Long userId, ReportRequest request, List<Long> walletIds) {
        // Convert LocalDateTime to Instant
        Instant startDate = request.getStartDate().atZone(ZoneId.systemDefault()).toInstant();
        Instant endDate = request.getEndDate().atZone(ZoneId.systemDefault()).toInstant();
        
        // Tạo query để lấy giao dịch với các điều kiện lọc
        return transactionRepository.findByUserIdAndDateRangeAndFilters(
                userId,
                startDate,
                endDate,
                walletIds,
                request.getTransactionTypes(),
                request.getCategoryIds()
        );
    }

    private BigDecimal calculateTotalByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ReportDataResponse.TransactionReportData mapToTransactionReportData(Transaction transaction) {
        return ReportDataResponse.TransactionReportData.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : "Không có danh mục")
                .walletName(transaction.getWallet().getName())
                .balanceAfterTransaction(transaction.getBalanceAfterTransaction())
                .build();
    }

    private List<ReportDataResponse.CategoryReportData> generateCategoryStats(List<Transaction> transactions, 
                                                                             BigDecimal totalIncome, 
                                                                             BigDecimal totalExpense) {
        Map<Long, List<Transaction>> transactionsByCategory = transactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(t -> t.getCategory().getId()));

        List<ReportDataResponse.CategoryReportData> categoryStats = new ArrayList<>();

        for (Map.Entry<Long, List<Transaction>> entry : transactionsByCategory.entrySet()) {
            Long categoryId = entry.getKey();
            List<Transaction> categoryTransactions = entry.getValue();
            
            Category category = categoryTransactions.get(0).getCategory();
            BigDecimal totalAmount = categoryTransactions.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            TransactionType type = categoryTransactions.get(0).getType();
            BigDecimal totalForType = type == TransactionType.INCOME ? totalIncome : totalExpense;
            BigDecimal percentage = totalForType.compareTo(BigDecimal.ZERO) > 0 
                ? totalAmount.divide(totalForType, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

            categoryStats.add(ReportDataResponse.CategoryReportData.builder()
                    .categoryId(categoryId)
                    .categoryName(category.getName())
                    .totalAmount(totalAmount)
                    .transactionCount(categoryTransactions.size())
                    .type(type)
                    .percentage(percentage)
                    .build());
        }

        return categoryStats.stream()
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .collect(Collectors.toList());
    }

    private List<ReportDataResponse.WalletReportData> generateWalletStats(List<Transaction> transactions, 
                                                                         List<Wallet> wallets) {
        Map<Long, List<Transaction>> transactionsByWallet = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getWallet().getId()));

        List<ReportDataResponse.WalletReportData> walletStats = new ArrayList<>();

        for (Wallet wallet : wallets) {
            List<Transaction> walletTransactions = transactionsByWallet.getOrDefault(wallet.getId(), new ArrayList<>());
            
            BigDecimal walletIncome = calculateTotalByType(walletTransactions, TransactionType.INCOME);
            BigDecimal walletExpense = calculateTotalByType(walletTransactions, TransactionType.EXPENSE);
            BigDecimal walletNetAmount = walletIncome.subtract(walletExpense);

            walletStats.add(ReportDataResponse.WalletReportData.builder()
                    .walletId(wallet.getId())
                    .walletName(wallet.getName())
                    .totalIncome(walletIncome)
                    .totalExpense(walletExpense)
                    .netAmount(walletNetAmount)
                    .transactionCount(walletTransactions.size())
                    .currentBalance(wallet.getBalance())
                    .build());
        }

        return walletStats;
    }
}
