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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví: " + request.getWalletId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục: " + request.getCategoryId()));

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

        boolean budgetExceeded = checkBudgetAndCreateNotification(user, category);

        TransactionResponse response = mapToTransactionResponse(savedTransaction);
        response.setBudgetExceeded(budgetExceeded);

        return response;
    }

    public List<TransactionResponse> getTransactions(Long userId, String type, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("date").descending());
        List<Transaction> transactions;

        if ("transfer".equalsIgnoreCase(type)) {
            transactions = transactionRepository.findTransferTransactionsByUserId(userId, pageable);
        } else if (type != null && !type.isBlank()) {
            transactions = transactionRepository.findByWallet_User_IdAndType(userId, TransactionType.valueOf(type.toUpperCase()), pageable);
        } else {
            List<TransactionType> types = List.of(TransactionType.INCOME, TransactionType.EXPENSE);
            transactions = transactionRepository.findByWallet_User_IdAndTypeInAndCategoryIsNotNull(userId, types, pageable);
        }

        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponse updateTransaction(Long transactionId, TransactionRequest request, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch: " + transactionId));

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!Objects.equals(transaction.getUser().getId(), userId)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa giao dịch này.");
        }

        Wallet oldWallet = transaction.getWallet();
        BigDecimal oldAmount = transaction.getAmount();
        TransactionType oldType = transaction.getType();
        Category oldCategory = transaction.getCategory();

        // Hoàn tác giao dịch cũ
        if (oldType == TransactionType.INCOME) {
            oldWallet.setBalance(oldWallet.getBalance().subtract(oldAmount));
        } else { // EXPENSE
            oldWallet.setBalance(oldWallet.getBalance().add(oldAmount));
        }

        Wallet newWallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví mới: " + request.getWalletId()));

        if (!Objects.equals(oldWallet.getId(), newWallet.getId())) {
            walletRepository.save(oldWallet);
        }

        Category newCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục mới: " + request.getCategoryId()));

        // Áp dụng giao dịch mới
        BigDecimal newAmount = request.getAmount();
        TransactionType newType = request.getType();
        if (newType == TransactionType.INCOME) {
            newWallet.setBalance(newWallet.getBalance().add(newAmount));
        } else { // EXPENSE
            newWallet.setBalance(newWallet.getBalance().subtract(newAmount));
        }
        walletRepository.save(newWallet);

        // Cập nhật thông tin giao dịch
        transaction.setAmount(newAmount);
        transaction.setType(newType);
        transaction.setWallet(newWallet);
        transaction.setCategory(newCategory);
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());

        Transaction updatedTransaction = transactionRepository.save(transaction);

        // Kiểm tra ngân sách cho cả danh mục cũ và mới
        boolean budgetExceeded = checkBudgetAndCreateNotification(user, newCategory);
        if(!Objects.equals(oldCategory.getId(), newCategory.getId())) {
            checkBudgetAndCreateNotification(user, oldCategory);
        }

        TransactionResponse response = mapToTransactionResponse(updatedTransaction);
        response.setBudgetExceeded(budgetExceeded);

        return response;
    }

    @Transactional
    public void deleteTransaction(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch: " + transactionId));

        if (!Objects.equals(transaction.getUser().getId(), userId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa giao dịch này.");
        }

        Wallet wallet = transaction.getWallet();
        BigDecimal amount = transaction.getAmount();
        Category category = transaction.getCategory();
        User user = transaction.getUser();

        // Hoàn tác giao dịch
        if (transaction.getType() == TransactionType.INCOME) {
            wallet.setBalance(wallet.getBalance().subtract(amount));
        } else { // EXPENSE
            wallet.setBalance(wallet.getBalance().add(amount));
        }

        walletRepository.save(wallet);
        transactionRepository.delete(transaction);

        // Kiểm tra lại ngân sách sau khi xóa
        if (category != null) {
            checkBudgetAndCreateNotification(user, category);
        }
    }

    private boolean checkBudgetAndCreateNotification(User user, Category category) {
        if (category != null && category.getBudgetAmount() != null && category.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
            YearMonth currentMonth = YearMonth.now();
            LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
            LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);

            BigDecimal totalSpent = transactionRepository.sumExpensesByCategoryIdAndDateRange(category.getId(), startDate, endDate);

            if (totalSpent != null && totalSpent.compareTo(category.getBudgetAmount()) >= 0) {
                notificationService.createBudgetExceededNotification(user, category.getName());
                return true;
            }
        }
        return false;
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

    public List<TransactionResponse> getTransactionsByCategoryId(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));
        if (!category.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập danh mục này.");
        }

        List<Transaction> transactions = transactionRepository.findByCategoryIdOrderByDateDesc(categoryId);
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }
}