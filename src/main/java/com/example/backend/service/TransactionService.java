package com.example.backend.service;

import com.example.backend.dto.request.DepositRequest;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.entity.Transaction;
import com.example.backend.entity.Wallet;
import com.example.backend.enums.PermissionType;
import com.example.backend.enums.TransactionType;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.WalletRepository;
import com.example.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletPermissionService walletPermissionService;
    private final WalletService walletService;

    /**
     * Xử lý nghiệp vụ nạp tiền vào ví.
     *
     * @param request      Thông tin yêu cầu nạp tiền.
     * @param currentUser  Thông tin người dùng đang đăng nhập.
     * @return Chi tiết của giao dịch vừa được tạo.
     */
    public TransactionResponse createDeposit(DepositRequest request, CustomUserDetails currentUser) {
        Long userId = currentUser.getId();
        Long walletId = request.getWalletId();

        // 1. Tìm ví từ DB
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + walletId));

        // 2. Kiểm tra quyền: User phải là chủ sở hữu hoặc có quyền ADD_TRANSACTION
        boolean isOwner = walletService.isWalletOwner(walletId, userId);
        boolean hasPermission = false;
        if (!isOwner) {
            hasPermission = walletPermissionService.hasPermission(walletId, userId, PermissionType.ADD_TRANSACTION);
        }

        if (!isOwner && !hasPermission) {
            log.warn("User {} không có quyền nạp tiền vào ví {}", userId, walletId);
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này trên ví.");
        }

        // 3. Cập nhật số dư ví
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);
        log.info("Đã cập nhật số dư cho ví {}. Số dư mới: {}", walletId, wallet.getBalance());

        // 4. Tạo và lưu giao dịch mới
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .user(currentUser.getUser()) // Gán user thực hiện giao dịch
                .amount(request.getAmount())
                .type(TransactionType.INCOME) // Giao dịch nạp tiền là INCOME
                .description(request.getDescription())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Đã tạo giao dịch nạp tiền mới với ID: {}", savedTransaction.getId());

        // 5. Xây dựng và trả về response
        return TransactionResponse.builder()
                .id(savedTransaction.getId())
                .amount(savedTransaction.getAmount())
                .type(savedTransaction.getType())
                .description(savedTransaction.getDescription())
                .transactionDate(savedTransaction.getTransactionDate())
                .walletId(savedTransaction.getWallet().getId())
                .userId(savedTransaction.getUser().getId())
                .build();
    }
}
