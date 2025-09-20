package com.example.backend.service;

import com.example.backend.dto.response.*;
import com.example.backend.entity.Transaction;
import com.example.backend.entity.Wallet;
import com.example.backend.enums.TransactionType;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final List<String> categoryColors = List.of("#3b82f6", "#10b981", "#f97316", "#a855f7", "#64748b", "#ef4444", "#eab308");

    public DashboardDataResponse getDashboardData(Long userId, Long walletId) {
        YearMonth currentMonth = YearMonth.now();
        Instant startOfMonth = currentMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);

        List<Wallet> allUserWallets = walletRepository.findByUserId(userId);
        if (allUserWallets.isEmpty() && walletId == null) {
            return createEmptyDashboardData();
        }

        List<Transaction> transactionsThisMonth;
        List<Transaction> recentTransactions;
        List<Wallet> walletsForSummary = (walletId != null)
                ? walletRepository.findByIdAndUserId(walletId, userId).map(List::of).orElse(List.of())
                : allUserWallets;

        if (walletId != null) {
            transactionsThisMonth = transactionRepository.findByWalletIdAndDateBetween(walletId, startOfMonth, endOfMonth);
            recentTransactions = transactionRepository.findTop5ByWalletIdOrderByDateDesc(walletId);
        } else {
            transactionsThisMonth = transactionRepository.findByWallet_UserIdAndDateBetween(userId, startOfMonth, endOfMonth);
            recentTransactions = transactionRepository.findTop5ByWallet_UserIdOrderByDateDesc(userId);
        }

        WalletSummaryResponse summary = calculateSummary(walletsForSummary, allUserWallets.size(), transactionsThisMonth);
        List<TransactionResponse> recentTransactionResponses = recentTransactions.stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
        List<CategorySpendingResponse> spendingByCategory = calculateSpendingByCategory(transactionsThisMonth);
        List<IncomeExpenseTrendResponse> incomeExpenseTrend = getIncomeExpenseTrend(userId, walletId, 9);
        List<ChartDataPointResponse> weeklySpending = getWeeklySpending(userId, walletId);
        List<CategorySpendingResponse> topSpendingCategories = spendingByCategory.stream().limit(3).collect(Collectors.toList());

        return DashboardDataResponse.builder()
                .summary(summary)
                .recentTransactions(recentTransactionResponses)
                .spendingByCategory(spendingByCategory)
                .incomeExpenseTrend(incomeExpenseTrend)
                .weeklySpending(weeklySpending)
                .topSpendingCategories(topSpendingCategories)
                .build();
    }

    private WalletSummaryResponse calculateSummary(List<Wallet> walletsForBalance, int totalWalletCount, List<Transaction> transactions) {
        BigDecimal totalBalance = walletsForBalance.stream()
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyIncome = calculateTotalByType(transactions, TransactionType.INCOME);
        BigDecimal monthlyExpense = calculateTotalByType(transactions, TransactionType.EXPENSE);

        BigDecimal netIncome = monthlyIncome.subtract(monthlyExpense);
        double monthlyGrowth = 0.0;
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            monthlyGrowth = netIncome.divide(monthlyIncome, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue();
        }

        return WalletSummaryResponse.builder()
                .totalBalanceVND(totalBalance)
                .monthlyIncome(monthlyIncome)
                .monthlyExpense(monthlyExpense)
                .totalWallets(totalWalletCount)
                .monthlyGrowth(monthlyGrowth)
                .totalTransactions(transactions.size())
                .build();
    }

    private List<IncomeExpenseTrendResponse> getIncomeExpenseTrend(Long userId, Long walletId, int months) {
        List<IncomeExpenseTrendResponse> trend = new ArrayList<>();
        YearMonth current = YearMonth.now();
        Locale vietnamese = new Locale("vi", "VN");
        for (int i = 0; i < months; i++) {
            YearMonth month = current.minusMonths(i);
            Instant start = month.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant end = month.atEndOfMonth().atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
            BigDecimal income = transactionRepository.sumAmountByUserAndTypeAndDateBetween(userId, walletId, TransactionType.INCOME, start, end);
            BigDecimal expense = transactionRepository.sumAmountByUserAndTypeAndDateBetween(userId, walletId, TransactionType.EXPENSE, start, end);
            String monthName = "Tháng " + month.getMonthValue();
            trend.add(new IncomeExpenseTrendResponse(monthName, income, expense));
        }
        Collections.reverse(trend);
        return trend;
    }

    private List<ChartDataPointResponse> getWeeklySpending(Long userId, Long walletId) {
        List<ChartDataPointResponse> weeklyData = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Locale vietnamese = new Locale("vi", "VN");
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.minusDays(i);
            Instant start = day.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant end = day.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
            BigDecimal expense = transactionRepository.sumAmountByUserAndTypeAndDateBetween(userId, walletId, TransactionType.EXPENSE, start, end);
            String dayName = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, vietnamese);
            weeklyData.add(new ChartDataPointResponse(dayName, expense));
        }
        Collections.reverse(weeklyData);
        return weeklyData;
    }

    private List<CategorySpendingResponse> calculateSpendingByCategory(List<Transaction> transactions) {
        final int[] colorIndex = {0};
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && t.getCategory() != null)
                .collect(Collectors.groupingBy(t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)))
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(entry -> {
                    String color = categoryColors.get(colorIndex[0] % categoryColors.size());
                    colorIndex[0]++;
                    return new CategorySpendingResponse(entry.getKey(), entry.getValue(), color);
                })
                .collect(Collectors.toList());
    }

    private TransactionResponse convertToTransactionResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .description(t.getDescription())
                .date(t.getDate())
                .category(t.getCategory() != null ? t.getCategory().getName() : "Không có")
                .walletName(t.getWallet() != null ? t.getWallet().getName() : "Không có")
                .build();
    }

    private DashboardDataResponse createEmptyDashboardData() {
        return DashboardDataResponse.builder()
                .summary(new WalletSummaryResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0.0, 0))
                .recentTransactions(Collections.emptyList())
                .spendingByCategory(Collections.emptyList())
                .incomeExpenseTrend(Collections.emptyList())
                .weeklySpending(Collections.emptyList())
                .topSpendingCategories(Collections.emptyList())
                .build();
    }

    private BigDecimal calculateTotalByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}