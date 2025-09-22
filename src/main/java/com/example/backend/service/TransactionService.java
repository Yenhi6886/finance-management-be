package com.example.backend.service;

import com.example.backend.dto.request.TransactionRequest;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.dto.response.TransactionStatisticResponse;
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
import com.example.backend.repository.WalletShareRepository;
import lombok.RequiredArgsConstructor;
import com.example.backend.service.WalletPermissionService;
import com.example.backend.enums.PermissionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
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
    private final WalletPermissionService walletPermissionService;
    private final WalletShareRepository walletShareRepository;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + userId));

        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví: " + request.getWalletId()));

        // Defensive permission check for shared wallets
        if (!walletPermissionService.hasPermission(wallet.getId(), userId, PermissionType.ADD_TRANSACTION)) {
            throw new AccessDeniedException("Bạn không có quyền thêm giao dịch cho ví này.");
        }

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

    public List<TransactionResponse> getTransactions(Long userId, String type, Long categoryId, LocalDate date, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date", "id"));
        List<Transaction> transactions;

        Instant startOfDay = (date != null) ? date.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant endOfDay = (date != null) ? date.atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toInstant() : null;

        if (startOfDay != null && categoryId != null) {
            transactions = transactionRepository.findByUser_IdAndCategoryIdAndDateBetweenOrderByDateDescIdDesc(userId, categoryId, startOfDay, endOfDay, pageable);
        } else if (startOfDay != null) {
            transactions = transactionRepository.findByUser_IdAndDateBetweenOrderByDateDescIdDesc(userId, startOfDay, endOfDay, pageable);
        } else if (categoryId != null) {
            transactions = transactionRepository.findByWallet_User_IdAndCategoryId(userId, categoryId, pageable);
        } else if ("transfer".equalsIgnoreCase(type)) {
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

        // Require EDIT permission on the target wallet
        if (!walletPermissionService.hasPermission(request.getWalletId(), userId, PermissionType.EDIT_TRANSACTION)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa giao dịch trên ví này.");
        }

        Wallet oldWallet = transaction.getWallet();
        BigDecimal oldAmount = transaction.getAmount();
        TransactionType oldType = transaction.getType();
        Category oldCategory = transaction.getCategory();

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

        BigDecimal newAmount = request.getAmount();
        TransactionType newType = request.getType();
        if (newType == TransactionType.INCOME) {
            newWallet.setBalance(newWallet.getBalance().add(newAmount));
        } else { // EXPENSE
            newWallet.setBalance(newWallet.getBalance().subtract(newAmount));
        }
        walletRepository.save(newWallet);

        transaction.setAmount(newAmount);
        transaction.setType(newType);
        transaction.setWallet(newWallet);
        transaction.setCategory(newCategory);
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());

        Transaction updatedTransaction = transactionRepository.save(transaction);

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

        // Require DELETE permission on the wallet of this transaction
        if (!walletPermissionService.hasPermission(transaction.getWallet().getId(), userId, PermissionType.DELETE_TRANSACTION)) {
            throw new AccessDeniedException("Bạn không có quyền xóa giao dịch trên ví này.");
        }

        Wallet wallet = transaction.getWallet();
        BigDecimal amount = transaction.getAmount();
        Category category = transaction.getCategory();
        User user = transaction.getUser();

        if (transaction.getType() == TransactionType.INCOME) {
            wallet.setBalance(wallet.getBalance().subtract(amount));
        } else { // EXPENSE
            wallet.setBalance(wallet.getBalance().add(amount));
        }

        walletRepository.save(wallet);
        transactionRepository.delete(transaction);

        if (category != null) {
            checkBudgetAndCreateNotification(user, category);
        }
    }

    private boolean checkBudgetAndCreateNotification(User user, Category category) {
        if (category != null && category.getBudgetAmount() != null && category.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
            YearMonth currentMonth = YearMonth.now();
            Instant startDate = currentMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

            BigDecimal totalSpent = transactionRepository.sumExpensesByCategoryIdAndDateRange(category.getId(), startDate, endDate);

            if (totalSpent != null && totalSpent.compareTo(category.getBudgetAmount()) >= 0) {
                notificationService.createBudgetExceededNotification(user, category.getName());
                return true;
            }
        }
        return false;
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

    public List<TransactionResponse> getTransactionsByCategoryId(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));
        if (!category.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền truy cập danh mục này.");
        }

        List<Transaction> transactions = transactionRepository.findByCategoryIdOrderByDateDescIdDesc(categoryId);
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    private TransactionStatisticResponse getTransactionStatistics(Long userId, Long walletId, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable, BigDecimal minAmount, BigDecimal maxAmount) {
        if (walletId != null) {
            Wallet wallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));
            if (!wallet.getUser().getId().equals(userId)) {
                throw new AccessDeniedException("Bạn không có quyền truy cập ví này.");
            }
        }

        Instant startDate = (startDateTime != null) ? startDateTime.toInstant(ZoneOffset.UTC) : null;
        Instant endDate = (endDateTime != null) ? endDateTime.toInstant(ZoneOffset.UTC) : null;

        Page<Transaction> transactionsPage;
        BigDecimal totalAmount;
        if (walletId != null) {
            transactionsPage = transactionRepository.getTransactionStatistics(userId, walletId, startDate, endDate, minAmount, maxAmount, pageable);
            totalAmount = transactionRepository.sumAmountForStatistics(userId, walletId, startDate, endDate, minAmount, maxAmount);
        } else {
            // Build allowed wallet ids: owned + shared accepted
            List<Long> ownedWalletIds = walletRepository.findByUserIdAndIsArchived(userId, false)
                    .stream().map(Wallet::getId).toList();
            List<Long> sharedWalletIds = walletShareRepository.findBySharedWithUserIdAndStatus(userId, com.example.backend.enums.InvitationStatus.ACCEPTED)
                    .stream().map(ws -> ws.getWallet().getId()).toList();
            List<Long> allowedWalletIds = new java.util.ArrayList<>();
            allowedWalletIds.addAll(ownedWalletIds);
            allowedWalletIds.addAll(sharedWalletIds);

            transactionsPage = transactionRepository.getTransactionStatisticsByWallets(allowedWalletIds, startDate, endDate, minAmount, maxAmount, pageable);
            totalAmount = transactionRepository.sumAmountForStatisticsByWallets(allowedWalletIds, startDate, endDate, minAmount, maxAmount);
        }

        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }

        Page<TransactionResponse> transactionResponsesPage = transactionsPage.map(this::mapToTransactionResponse);

        return new TransactionStatisticResponse(transactionResponsesPage, totalAmount);
    }

    public TransactionStatisticResponse getTransactionsToday(Long userId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return getTransactionStatistics(userId, null, startOfDay, endOfDay, pageable, null, null);
    }

    public TransactionStatisticResponse getTransactionsByTime(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable, BigDecimal minAmount, BigDecimal maxAmount) {
        return getTransactionStatistics(userId, null, startDate, endDate, pageable, minAmount, maxAmount);
    }

    public TransactionStatisticResponse getTransactionsTodayByWalletId(Long userId, Long walletId, Pageable pageable) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return getTransactionStatistics(userId, walletId, startOfDay, endOfDay, pageable, null, null);
    }

    public TransactionStatisticResponse getTransactionsByWalletIdAndTime(Long userId, Long walletId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable, BigDecimal minAmount, BigDecimal maxAmount) {
        return getTransactionStatistics(userId, walletId, startDate, endDate, pageable, minAmount, maxAmount);
    }
}