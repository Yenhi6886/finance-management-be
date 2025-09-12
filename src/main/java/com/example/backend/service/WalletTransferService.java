package com.example.backend.service;

import com.example.backend.dto.request.WalletTransferRequest;
import com.example.backend.dto.response.WalletTransferResponse;
import com.example.backend.dto.request.UpdateProfileRequest;
import com.example.backend.entity.Wallet;
import com.example.backend.exception.InsufficientBalanceException;
import com.example.backend.exception.WalletNotFoundException;
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

    @Transactional
    public WalletTransferResponse transferMoney(Long userId, WalletTransferRequest request) {
        // Validate input
        if (request.getFromWalletId().equals(request.getToWalletId())) {
            throw new IllegalArgumentException("Không thể chuyển tiền trong cùng một ví");
        }

        // Lấy thông tin ví nguồn
        Wallet fromWallet = walletRepository.findByIdAndUserId(request.getFromWalletId(), userId)
            .orElseThrow(() -> new WalletNotFoundException("Không tìm thấy ví nguồn"));

        // Lấy thông tin ví đích
        Wallet toWallet = walletRepository.findByIdAndUserId(request.getToWalletId(), userId)
            .orElseThrow(() -> new WalletNotFoundException("Không tìm thấy ví đích"));

        // Kiểm tra số dư ví nguồn
        if (fromWallet.getInitialBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                String.format("Số dư không đủ. Số dư hiện tại: %s %s, Số tiền cần chuyển: %s %s",
                    fromWallet.getInitialBalance(), fromWallet.getCurrency(),
                    request.getAmount(), fromWallet.getCurrency())
            );
        }

        // Kiểm tra cùng loại tiền tệ
        if (!fromWallet.getCurrency().equals(toWallet.getCurrency())) {
            throw new IllegalArgumentException("Hai ví phải cùng loại tiền tệ");
        }

        // Thực hiện chuyển tiền
        BigDecimal newFromBalance = fromWallet.getInitialBalance().subtract(request.getAmount());
        BigDecimal newToBalance = toWallet.getInitialBalance().add(request.getAmount());

        fromWallet.setInitialBalance(newFromBalance);
        toWallet.setInitialBalance(newToBalance);

        // Lưu thay đổi
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Trả về kết quả
        return WalletTransferResponse.builder()
            .message("Chuyển tiền thành công")
            .fromWalletBalance(newFromBalance)
            .toWalletBalance(newToBalance)
            .transferTime(LocalDateTime.now())
            .success(true)
            .build();
    }

    public boolean validateTransferAmount(Long walletId, Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
            .orElseThrow(() -> new WalletNotFoundException("Không tìm thấy ví"));
        return wallet.getInitialBalance().compareTo(amount) >= 0;
    }
}

