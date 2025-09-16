package com.example.backend.service;

import com.example.backend.dto.request.ExpenseCreateRequest;
import com.example.backend.dto.request.ExpenseUpdateRequest;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.entity.Transaction;
import com.example.backend.entity.TransactionCategory;
import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import com.example.backend.enums.Currency;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.TransactionCategoryRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;

    @Transactional
    public TransactionResponse createExpense(ExpenseCreateRequest request, Long userId) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Validate wallet exists and belongs to user or user has permission
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + request.getWalletId()));

        // Validate category exists
        TransactionCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        // Lấy currency của request, mặc định là VND
        Currency transactionCurrency = request.getCurrency() != null ? request.getCurrency() : Currency.VND;

        // Kiểm tra số dư ví có đủ không (sau khi quy đổi tiền tệ nếu cần)
        if (!currencyService.hasSufficientBalance(wallet.getBalance(), wallet.getCurrency(),
                request.getAmount(), transactionCurrency, userId)) {
            throw new BadRequestException("Số dư ví không đủ để thực hiện giao dịch này");
        }

        // Quy đổi số tiền giao dịch sang đơn vị tiền tệ của ví
        BigDecimal convertedAmount = currencyService.convertToWalletCurrency(
                request.getAmount(), transactionCurrency, wallet.getCurrency(), userId);

        // Trừ số tiền đã quy đổi từ ví
        wallet.setBalance(wallet.getBalance().subtract(convertedAmount));
        walletRepository.save(wallet);

        // Tạo transaction với số tiền gốc (chưa quy đổi) và currency của giao dịch
        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .category(category)
                .wallet(wallet)
                .user(user)
                .description(request.getDescription())
                .date(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Return response với thông tin giao dịch
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
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Create pageable for limiting results
        Pageable pageable = PageRequest.of(0, limit > 0 ? limit : 10);

        // Get transactions by user ID
        List<Transaction> transactions = transactionRepository.findByWallet_User_Id(userId, pageable);

        // Convert to response DTOs
        return transactions.stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getAllTransactions(Long userId) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Get all transactions without limit
        List<Transaction> transactions = transactionRepository.findByWallet_User_Id(userId, Pageable.unpaged());

        // Convert to response DTOs
        return transactions.stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse convertToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .walletId(transaction.getWallet().getId())
                .walletName(transaction.getWallet().getName())
                .categoryName(transaction.getCategory().getName())
                .build();
    }
    public TransactionResponse updateExpense(Long expenseId, ExpenseUpdateRequest request, Long userId) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Validate expense exists and belongs to user
        Transaction existingExpense = transactionRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoản chi với ID: " + expenseId));

        if (!existingExpense.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền chỉnh sửa khoản chi này");
        }

        // Lấy ví cũ và ví mới
        Wallet oldWallet = existingExpense.getWallet();
        Wallet newWallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + request.getWalletId()));

        // Validate category exists
        TransactionCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        // Lấy currency của request, mặc định là VND
        Currency transactionCurrency = request.getCurrency() != null ? request.getCurrency() : Currency.VND;

        // Tính số tiền đã quy đổi của giao dịch cũ theo currency của ví cũ
        BigDecimal oldConvertedAmount = currencyService.convertToWalletCurrency(
                existingExpense.getAmount(), transactionCurrency, oldWallet.getCurrency(), userId);

        // Hoàn tiền vào ví cũ
        oldWallet.setBalance(oldWallet.getBalance().add(oldConvertedAmount));
        walletRepository.save(oldWallet);

        // Tính số tiền mới cần trừ từ ví mới
        BigDecimal newConvertedAmount = currencyService.convertToWalletCurrency(
                request.getAmount(), transactionCurrency, newWallet.getCurrency(), userId);

        // Kiểm tra số dư ví mới có đủ không
        if (!currencyService.hasSufficientBalance(newWallet.getBalance(), newWallet.getCurrency(),
                request.getAmount(), transactionCurrency, userId)) {
            // Rollback: hoàn lại trạng thái ví cũ
            oldWallet.setBalance(oldWallet.getBalance().subtract(oldConvertedAmount));
            walletRepository.save(oldWallet);
            throw new BadRequestException("Số dư ví mới không đủ để thực hiện giao dịch này");
        }

        // Trừ tiền từ ví mới
        newWallet.setBalance(newWallet.getBalance().subtract(newConvertedAmount));
        walletRepository.save(newWallet);

        // Cập nhật thông tin giao dịch
        existingExpense.setAmount(request.getAmount());
        existingExpense.setCategory(category);
        existingExpense.setWallet(newWallet);
        existingExpense.setDescription(request.getDescription());
        existingExpense.setDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now());
        Transaction updatedTransaction = transactionRepository.save(existingExpense);

        // Return response với thông tin giao dịch đã cập nhật
        return TransactionResponse.builder()
                .id(updatedTransaction.getId())
                .amount(updatedTransaction.getAmount())
                .description(updatedTransaction.getDescription())
                .date(updatedTransaction.getDate())
                .walletId(newWallet.getId())
                .walletName(newWallet.getName())
                .categoryName(category.getName())
                .build();
    }

    public void deleteTransaction(Long transactionId, Long userId) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        // Validate transaction exists and belongs to user
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch với ID: " + transactionId));
        if (!transaction.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền xóa giao dịch này");
        }
        // Hoàn tiền giao dịch nếu là khoản chi
        Wallet wallet = transaction.getWallet();
        BigDecimal convertedAmount = currencyService.convertToWalletCurrency(
                transaction.getAmount(), Currency.VND, wallet.getCurrency(), userId);
        wallet.setBalance(wallet.getBalance().add(convertedAmount));
        walletRepository.save(wallet);
        // Xóa giao dịch
        transactionRepository.delete(transaction);

}
}
