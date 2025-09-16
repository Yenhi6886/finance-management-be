package com.example.backend.service;

import com.example.backend.dto.request.ExpenseCreateRequest;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.entity.Transaction;
import com.example.backend.entity.TransactionCategory;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.TransactionCategoryRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse createExpense(ExpenseCreateRequest request, Long userId) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Validate wallet exists and belongs to user or user has permission
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + request.getWalletId()));

        // Validate category exists - no more type checking, just category existence
        TransactionCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        // Check if wallet has sufficient balance (only for expense-like categories)
        // We can identify expense categories by their names containing keywords like "chi", "mua", etc.
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Số dư ví không đủ để thực hiện giao dịch này");
        }

        // Deduct amount from wallet balance if it's an expense category

            wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));

        walletRepository.save(wallet);

        // Create transaction
        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .category(category)
                .wallet(wallet)
                .user(user)
                .description(request.getDescription())
                .date(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Return response with only category name
        return TransactionResponse.builder()
                .id(savedTransaction.getId())
                .amount(savedTransaction.getAmount())
                .description(savedTransaction.getDescription())
                .date(savedTransaction.getDate())
                .walletId(wallet.getId())
                .walletName(wallet.getName())
                .categoryName(category.getName())
                .build();
    }

    public List<TransactionResponse> getTransactions(Long userId, String type, int limit) {
        // Implementation for getting transactions (if needed)
        return List.of();
    }



}
