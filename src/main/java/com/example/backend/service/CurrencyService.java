package com.example.backend.service;

import com.example.backend.entity.UserSettings;
import com.example.backend.enums.Currency;
import com.example.backend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final UserSettingsRepository userSettingsRepository;

    // Tỷ giá mặc định nếu không tìm thấy trong UserSettings
    private static final BigDecimal DEFAULT_USD_TO_VND_RATE = new BigDecimal("24000");

    /**
     * Lấy tỷ giá USD/VND từ UserSettings của user
     */
    private BigDecimal getUsdToVndRate(Long userId) {
        UserSettings userSettings = userSettingsRepository.findById(userId).orElse(null);
        if (userSettings != null && userSettings.getUsdToVndRate() != null) {
            return userSettings.getUsdToVndRate();
        }
        return DEFAULT_USD_TO_VND_RATE;
    }

    /**
     * Quy đổi số tiền từ loại tiền tệ này sang loại tiền tệ khác
     */
    public BigDecimal convertCurrency(BigDecimal amount, Currency fromCurrency, Currency toCurrency, Long userId) {
        if (fromCurrency == toCurrency) {
            return amount;
        }

        BigDecimal usdToVndRate = getUsdToVndRate(userId);

        if (fromCurrency == Currency.USD && toCurrency == Currency.VND) {
            return amount.multiply(usdToVndRate).setScale(0, RoundingMode.HALF_UP);
        } else if (fromCurrency == Currency.VND && toCurrency == Currency.USD) {
            return amount.divide(usdToVndRate, 4, RoundingMode.HALF_UP);
        }

        return amount;
    }

    /**
     * Quy đổi số tiền giao dịch sang đơn vị tiền tệ của ví
     */
    public BigDecimal convertToWalletCurrency(BigDecimal transactionAmount, Currency transactionCurrency, Currency walletCurrency, Long userId) {
        return convertCurrency(transactionAmount, transactionCurrency, walletCurrency, userId);
    }

    /**
     * Kiểm tra số dư ví có đủ cho giao dịch không (sau khi quy đổi tiền tệ)
     */
    public boolean hasSufficientBalance(BigDecimal walletBalance, Currency walletCurrency,
                                      BigDecimal transactionAmount, Currency transactionCurrency, Long userId) {
        BigDecimal convertedAmount = convertToWalletCurrency(transactionAmount, transactionCurrency, walletCurrency, userId);
        return walletBalance.compareTo(convertedAmount) >= 0;
    }
}
