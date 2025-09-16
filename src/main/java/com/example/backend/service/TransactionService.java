package com.example.backend.service;

import com.example.backend.dto.request.TransactionRequest;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.entity.Category;
import com.example.backend.entity.Transaction;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.enums.TransactionType;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví: " + request.getWalletId()));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục: " + request.getCategoryId()));
        }

        BigDecimal amount = request.getAmount();
        BigDecimal newBalance;

        if (request.getType() == TransactionType.INCOME) {
            newBalance = wallet.getBalance().add(amount);
        } else if (request.getType() == TransactionType.EXPENSE) {
            newBalance = wallet.getBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Số dư không đủ.");
            }
        } else {
            throw new BadRequestException("Loại giao dịch không hợp lệ.");
        }

        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .type(request.getType())
                .wallet(wallet)
                .user(user)
                .category(category)
                .description(request.getDescription())
                .date(request.getDate())
                .balanceAfterTransaction(newBalance)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToTransactionResponse(savedTransaction);
    }

    public List<TransactionResponse> getTransactions(Long userId, String type, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("date").descending());
        List<Transaction> transactions;

        if ("transfer".equalsIgnoreCase(type)) {
            transactions = transactionRepository.findTransferTransactionsByUserId(userId, pageable);
        } else if (type != null && !type.isBlank()) {
            transactions = transactionRepository.findByWallet_User_IdAndType(userId, TransactionType.valueOf(type.toUpperCase()), pageable);
        } else {
            transactions = transactionRepository.findByWallet_User_Id(userId, pageable);
        }

        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        TransactionResponse.TransactionResponseBuilder builder = TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount().abs())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .date(transaction.getDate())
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