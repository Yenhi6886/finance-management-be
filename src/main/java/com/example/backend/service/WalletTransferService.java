package com.example.backend.service;

import com.example.backend.dto.request.WalletTransferRequest;
import com.example.backend.dto.response.WalletTransferResponse;
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
    private final ExchangeRateService exchangeRateService;

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

        // Kiểm tra loại tiền tệ có được hỗ trợ không
        if (!exchangeRateService.isSupportedCurrency(fromWallet.getCurrency()) ||
            !exchangeRateService.isSupportedCurrency(toWallet.getCurrency())) {
            throw new IllegalArgumentException("Loại tiền tệ không được hỗ trợ");
        }

        // Chuyển đổi số tiền cần chuyển về USD để validate
        BigDecimal requestAmountInUSD = exchangeRateService.convertToUSD(request.getAmount(), fromWallet.getCurrency());
        BigDecimal fromWalletBalanceInUSD = exchangeRateService.convertToUSD(fromWallet.getBalance(), fromWallet.getCurrency());

        // Kiểm tra số dư ví nguồn (so sánh theo USD)
        if (fromWalletBalanceInUSD.compareTo(requestAmountInUSD) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Số dư không đủ. Số dư hiện tại: %s %s (~%s USD), Số tiền cần chuyển: %s %s (~%s USD)",
                            fromWallet.getBalance(), fromWallet.getCurrency(), fromWalletBalanceInUSD,
                            request.getAmount(), fromWallet.getCurrency(), requestAmountInUSD)
            );
        }

        // Tính toán số tiền thực tế sẽ được trừ và cộng
        BigDecimal amountToSubtract = request.getAmount(); // Số tiền trừ từ ví nguồn (theo currency của ví nguồn)
        BigDecimal amountToAdd; // Số tiền cộng vào ví đích (theo currency của ví đích)

        if (fromWallet.getCurrency().equals(toWallet.getCurrency())) {
            // Cùng loại tiền tệ
            amountToAdd = request.getAmount();
        } else {
            // Khác loại tiền tệ - cần chuyển đổi
            amountToAdd = exchangeRateService.convertCurrency(request.getAmount(), fromWallet.getCurrency(), toWallet.getCurrency());
        }

        // Thực hiện chuyển tiền
        BigDecimal newFromBalance = fromWallet.getBalance().subtract(amountToSubtract);
        BigDecimal newToBalance = toWallet.getBalance().add(amountToAdd);

        fromWallet.setBalance(newFromBalance);
        toWallet.setBalance(newToBalance);

        // Lưu thay đổi
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Tạo message thông báo
        String message;
        if (fromWallet.getCurrency().equals(toWallet.getCurrency())) {
            message = String.format("Chuyển tiền thành công: %s %s", request.getAmount(), fromWallet.getCurrency());
        } else {
            // Tính tỷ giá trực tiếp giữa 2 loại tiền tệ
            BigDecimal directRate = amountToAdd.divide(request.getAmount(), 4, java.math.RoundingMode.HALF_UP);
            message = String.format("Chuyển tiền thành công: %s %s → %s %s (tỷ giá: 1 %s = %s %s)",
                    request.getAmount(), fromWallet.getCurrency(),
                    amountToAdd, toWallet.getCurrency(),
                    fromWallet.getCurrency(), directRate, toWallet.getCurrency());
        }

        // Trả về kết quả
        return WalletTransferResponse.builder()
                .message(message)
                .fromWalletBalance(newFromBalance)
                .toWalletBalance(newToBalance)
                .transferTime(LocalDateTime.now())
                .success(true)
                .build();
    }

    public boolean validateTransferAmount(Long walletId, Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Không tìm thấy ví"));

        // Validate theo USD
        BigDecimal amountInUSD = exchangeRateService.convertToUSD(amount, wallet.getCurrency());
        BigDecimal balanceInUSD = exchangeRateService.convertToUSD(wallet.getBalance(), wallet.getCurrency());

        return balanceInUSD.compareTo(amountInUSD) >= 0;
    }
}