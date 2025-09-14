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

    public List<Wallet> listUserWallets(Long userId) {
        return  walletRepository.findAllByUserId(userId);
    }

    //lay ID vi hien tai dang duoc chon
    public Optional<Long> getCurrentSelectedWalletId(Long userId) {
        return userSettingsRepository.findById(userId)
                .map(settings -> settings.getCurrentWallet() != null ? settings.getCurrentWallet().getId() : null);
    }

    //lay thong tin vi hien tai dang duoc chon
    public Optional<Wallet> getCurrentSelectedWallet(Long userId) {
        return userSettingsRepository.findById(userId)
                .map(settings -> settings.getCurrentWallet());
    }

    //dat vi hien tai dang duoc chon
    @Transactional
    public void setCurrentSelectedWallet(Long userId, Long walletId) {
        //kiem tra vi co thuoc ve user khong
        boolean walletExists = walletRepository.existsByIdAndUserId(walletId, userId);
        if (!walletExists) {
            throw new IllegalArgumentException("Wallet does not found or does not belong to user");
        }
        //lay hoac tao moi user settings
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseGet(() -> {
                    UserSettings newSettings = UserSettings.builder().user(userRepository.getReferenceById(userId)).build();
                    return newSettings;
                });

        //cap nhat vi hien tai
        settings.setCurrentWallet(walletRepository.getReferenceById(walletId));
        userSettingsRepository.save(settings);
    }

    // Tính tổng số dư theo từng loại tiền tệ
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

    // Tính tổng số dư quy đổi về VND (giả sử tỷ giá cố định)
    public BigDecimal getTotalBalanceInVND(Long userId) {
        List<Wallet> wallets = listUserWallets(userId);
        BigDecimal totalVND = BigDecimal.ZERO;

        for (Wallet wallet : wallets) {
            BigDecimal balanceInVND = convertToVND(wallet.getBalance(), wallet.getCurrency());
            totalVND = totalVND.add(balanceInVND);
        }

        return totalVND;
    }

    // Phương thức quy đổi tiền tệ về VND (có thể tùy chỉnh tỷ giá)
    private BigDecimal convertToVND(BigDecimal amount, com.example.backend.enums.Currency currency) {
        return switch (currency) {
            case VND -> amount;
            case USD -> amount.multiply(new BigDecimal("24000")); // 1 USD = 24,000 VND
            default -> amount; // Mặc định giữ nguyên
        };
    }
}