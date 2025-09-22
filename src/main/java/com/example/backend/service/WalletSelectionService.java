package com.example.backend.service;

import com.example.backend.entity.UserSettings;
import com.example.backend.entity.Wallet;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserSettingsRepository;
import com.example.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletSelectionService {
    private final WalletRepository walletRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;
    private static final BigDecimal DEFAULT_USD_VND_RATE = new BigDecimal("25400.00");

    public List<Wallet> listUserWallets(Long userId) {
        return  walletRepository.findAllByUserId(userId);
    }

    public Optional<Long> getCurrentSelectedWalletId(Long userId) {
        return userSettingsRepository.findById(userId)
                .map(settings -> settings.getCurrentWallet() != null ? settings.getCurrentWallet().getId() : null);
    }

    public Optional<Wallet> getCurrentSelectedWallet(Long userId) {
        return userSettingsRepository.findById(userId)
                .map(settings -> settings.getCurrentWallet());
    }

    @Transactional
    public void setCurrentSelectedWallet(Long userId, Long walletId) {
        boolean walletExists = walletRepository.existsByIdAndUserId(walletId, userId);
        if (!walletExists) {
            throw new IllegalArgumentException("Wallet does not found or does not belong to user");
        }
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseGet(() -> {
                    UserSettings newSettings = UserSettings.builder().user(userRepository.getReferenceById(userId)).build();
                    return newSettings;
                });

        settings.setCurrentWallet(walletRepository.getReferenceById(walletId));
        userSettingsRepository.save(settings);
    }

    public Map<String, BigDecimal> getTotalBalanceByCurrency(Long userId) {
        List<Wallet> wallets = listUserWallets(userId);
        return wallets.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        wallet -> wallet.getCurrency().name(),
                        java.util.stream.Collectors.reducing(
                                BigDecimal.ZERO,
                                Wallet::getBalance,
                                BigDecimal::add
                        )
                ));
    }

    public BigDecimal getTotalBalanceInVND(Long userId) {
        List<Wallet> wallets = listUserWallets(userId);
        BigDecimal totalVND = BigDecimal.ZERO;

        BigDecimal exchangeRate = userSettingsRepository.findById(userId)
                .map(UserSettings::getUsdToVndRate)
                .orElse(DEFAULT_USD_VND_RATE);

        for (Wallet wallet : wallets) {
            BigDecimal balanceInVND = convertToVND(wallet.getBalance(), wallet.getCurrency(), exchangeRate);
            totalVND = totalVND.add(balanceInVND);
        }

        return totalVND;
    }

    private BigDecimal convertToVND(BigDecimal amount, com.example.backend.enums.Currency currency, BigDecimal exchangeRate) {
        return switch (currency) {
            case VND -> amount;
            case USD -> amount.multiply(exchangeRate);
            default -> amount;
        };
    }
}