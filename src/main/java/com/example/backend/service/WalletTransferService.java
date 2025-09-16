package com.example.backend.service;

import com.example.backend.dto.request.WalletTransferRequest;
import com.example.backend.dto.response.WalletTransferResponse;
import com.example.backend.entity.Transaction;
import com.example.backend.entity.TransactionCategory;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.exception.InsufficientBalanceException;
import com.example.backend.exception.WalletNotFoundException;
import com.example.backend.repository.TransactionCategoryRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletTransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public WalletTransferResponse transferMoney(Long userId, WalletTransferRequest request) {
        if (request.getFromWalletId().equals(request.getToWalletId())) {
            throw new IllegalArgumentException("Không thể chuyển tiền trong cùng một ví.");
        }

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet fromWallet = walletRepository.findById(request.getFromWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Không tìm thấy ví nguồn."));

        Wallet toWallet = walletRepository.findById(request.getToWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Không tìm thấy ví đích."));

        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Số dư ví nguồn không đủ.");
        }

        if (!fromWallet.getCurrency().equals(toWallet.getCurrency())) {
            throw new IllegalArgumentException("Chỉ có thể chuyển tiền giữa các ví cùng loại tiền tệ.");
        }

        LocalDateTime transactionTime = (request.getDate() != null) ? request.getDate() : LocalDateTime.now();

        // Get or create categories for transfer operations
        TransactionCategory transferCategory = getOrCreateTransferCategory();
        TransactionCategory expenseCategory = getOrCreateExpenseCategory();
        TransactionCategory incomeCategory = getOrCreateIncomeCategory();

        // 1. Tạo giao dịch cha (meta-transaction) with transfer category
        Transaction parentTransaction = Transaction.builder()
                .user(currentUser)
                .wallet(fromWallet)
                .category(transferCategory)
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(transactionTime)
                .build();
        Transaction savedParent = transactionRepository.save(parentTransaction);

        // 2. Tạo giao dịch con EXPENSE
        Transaction expenseTransaction = Transaction.builder()
                .parentTransaction(savedParent)
                .user(currentUser)
                .wallet(fromWallet)
                .category(expenseCategory)
                .amount(request.getAmount().negate())
                .description(String.format("Chi tiết chuyển tiền đến ví '%s'", toWallet.getName()))
                .date(transactionTime)
                .build();

        // 3. Tạo giao dịch con INCOME
        Transaction incomeTransaction = Transaction.builder()
                .parentTransaction(savedParent)
                .user(toWallet.getUser())
                .wallet(toWallet)
                .category(incomeCategory)
                .amount(request.getAmount())
                .description(String.format("Chi tiết nhận tiền từ ví '%s'", fromWallet.getName()))
                .date(transactionTime)
                .build();

        transactionRepository.save(expenseTransaction);
        transactionRepository.save(incomeTransaction);

        // 4. Cập nhật số dư
        fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));
        toWallet.setBalance(toWallet.getBalance().add(request.getAmount()));
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        return WalletTransferResponse.builder()
                .message("Chuyển tiền thành công")
                .fromWalletBalance(fromWallet.getBalance())
                .toWalletBalance(toWallet.getBalance())
                .transferTime(LocalDateTime.now())
                .build();
    }

    private TransactionCategory getOrCreateTransferCategory() {
        return transactionCategoryRepository.findByName("Chuyển khoản")
                .stream()
                .findFirst()
                .orElseGet(() -> transactionCategoryRepository.save(
                        TransactionCategory.builder()
                                .name("Chuyển khoản")
                                .description("Danh mục cho giao dịch chuyển tiền giữa các ví")
                                .budget(BigDecimal.ZERO)
                                .build()
                ));
    }

    private TransactionCategory getOrCreateExpenseCategory() {
        return transactionCategoryRepository.findByNameContainingIgnoreCase("chi")
                .stream()
                .findFirst()
                .orElseGet(() -> transactionCategoryRepository.save(
                        TransactionCategory.builder()
                                .name("Chi tiêu - Chuyển khoản")
                                .description("Danh mục cho việc chi tiền khi chuyển khoản")
                                .budget(BigDecimal.ZERO)
                                .build()
                ));
    }

    private TransactionCategory getOrCreateIncomeCategory() {
        return transactionCategoryRepository.findByNameContainingIgnoreCase("thu")
                .stream()
                .findFirst()
                .orElseGet(() -> transactionCategoryRepository.save(
                        TransactionCategory.builder()
                                .name("Thu nhập - Chuyển khoản")
                                .description("Danh mục cho việc nhận tiền từ chuyển khoản")
                                .budget(BigDecimal.ZERO)
                                .build()
                ));
    }

    public boolean validateTransferAmount(Long walletId, Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Không tìm thấy ví"));
        return wallet.getBalance().compareTo(amount) >= 0;
    }
}