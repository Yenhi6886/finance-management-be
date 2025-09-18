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
import java.time.LocalDateTime;
import java.time.YearMonth;
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
        LocalDateTime startDay = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDay = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal totalBudget = categoryRepository.sumBudgetAmountByUserId(userId);

        BigDecimal totalIncome = transactionRepository.sumAmountByTypeAndDateBetween(
                userId, TransactionType.INCOME, startDay, endDay);

        BigDecimal totalExpense = transactionRepository.sumAmountByTypeAndDateBetween(
                userId, TransactionType.EXPENSE, startDay, endDay);

        BigDecimal remainingAmount = totalBudget.subtract(totalExpense).add(totalIncome);

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        Page<Transaction> transactions =  transactionRepository.findAllByUserIdAndDateBetween(userId, startDay, endDay, pageable);

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
        TransactionResponse.TransactionResponseBuilder builder = TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount().abs())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .category(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .walletId(transaction.getWallet().getId())
                .walletName(transaction.getWallet().getName());

        if (transaction.getType() == TransactionType.TRANSFER) {
            List<Long> walletIds = List.of(transaction.getFromWalletId(), transaction.getToWalletId());
            Map<Long, String> walletNames = walletRepository.findAllById(walletIds).stream()
                    .collect(Collectors.toMap(Wallet::getId, Wallet::getName));

            builder.fromWalletName(walletNames.get(transaction.getFromWalletId()));
            builder.toWalletName(walletNames.get(transaction.getToWalletId()));
        }
        return builder.build();
    }
}
