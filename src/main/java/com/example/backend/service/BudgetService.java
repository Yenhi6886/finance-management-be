package com.example.backend.service;

import com.example.backend.dto.response.BudgetStatRespond;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.entity.Transaction;
import com.example.backend.entity.Wallet;
import com.example.backend.enums.TransactionType;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final WalletRepository walletRepository;

    public BudgetStatRespond getBudgetStat(Long userId, int year, int month, int page, int size) {
        YearMonth yearMonth = YearMonth.of(year, month);
       Instant startDay = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endDay = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        Instant startInstant = startDay.toInstant(ZoneOffset.UTC);
        Instant endInstant = endDay.toInstant(ZoneOffset.UTC);

        BigDecimal totalBudget = categoryRepository.sumBudgetAmountByUserId(userId);
        if (totalBudget == null) {
            totalBudget = BigDecimal.ZERO;
        }

        BigDecimal totalIncome = transactionRepository.sumAmountByTypeAndDateBetween(
                userId, TransactionType.INCOME, startInstant, endInstant);
        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }

        BigDecimal totalExpense = transactionRepository.sumAmountByTypeAndDateBetween(
                userId, TransactionType.EXPENSE, startInstant, endInstant);
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount = totalBudget.subtract(totalExpense).add(totalIncome);

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        Page<Transaction> transactions =  transactionRepository.findAllByUserIdAndDateBetween(userId, startInstant, endInstant, pageable);

        Page<TransactionResponse> transactionResponses =  transactions.map(this::mapToTransactionResponse);

        return BudgetStatRespond.builder()
                .totalBudget(totalBudget)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .remainingAmount(remainingAmount)
                .transactions(transactionResponses)
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        BigDecimal amount = transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount().abs();

        TransactionResponse.TransactionResponseBuilder builder = TransactionResponse.builder()
                .id(transaction.getId())
                .amount(amount)
                .type(transaction.getType())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .category(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .walletId(transaction.getWallet() != null ? transaction.getWallet().getId() : null)
                .walletName(transaction.getWallet() != null ? transaction.getWallet().getName() : null);

        if (transaction.getType() == TransactionType.TRANSFER) {
            Long fromWalletId = transaction.getFromWalletId();
            Long toWalletId = transaction.getToWalletId();

            if (fromWalletId != null && toWalletId != null) {
                List<Long> walletIds = List.of(fromWalletId, toWalletId);
                Map<Long, String> walletNames = walletRepository.findAllById(walletIds).stream()
                        .collect(Collectors.toMap(Wallet::getId, Wallet::getName));

                builder.fromWalletName(walletNames.get(fromWalletId));
                builder.toWalletName(walletNames.get(toWalletId));
            }
        }
        return builder.build();
    }
}