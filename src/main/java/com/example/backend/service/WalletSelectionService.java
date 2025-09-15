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
    private final ExchangeRateService exchangeRateService;

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

    // Tính tổng số dư quy đổi về VND sử dụng tỷ giá động
    public BigDecimal getTotalBalanceInVND(Long userId) {
        List<Wallet> wallets = listUserWallets(userId);
        BigDecimal totalVND = BigDecimal.ZERO;

        for (Wallet wallet : wallets) {
            BigDecimal balanceInVND = exchangeRateService.convertCurrency(
                wallet.getBalance(),
                wallet.getCurrency(),
                com.example.backend.enums.Currency.VND
            );
            totalVND = totalVND.add(balanceInVND);
        }

        return totalVND;
    }

    // Tính tổng số dư quy đổi về USD
    public BigDecimal getTotalBalanceInUSD(Long userId) {
        List<Wallet> wallets = listUserWallets(userId);
        BigDecimal totalUSD = BigDecimal.ZERO;

        for (Wallet wallet : wallets) {
            BigDecimal balanceInUSD = exchangeRateService.convertToUSD(wallet.getBalance(), wallet.getCurrency());
            totalUSD = totalUSD.add(balanceInUSD);
        }

        return totalUSD;
    }

    // Lấy thông tin tổng số dư theo tất cả loại tiền tệ
    public Map<String, Object> getWalletSummary(Long userId) {
        Map<String, BigDecimal> balanceByCurrency = getTotalBalanceByCurrency(userId);
        BigDecimal totalVND = getTotalBalanceInVND(userId);
        BigDecimal totalUSD = getTotalBalanceInUSD(userId);

        return Map.of(
            "balanceByCurrency", balanceByCurrency,
            "totalVND", totalVND,
            "totalUSD", totalUSD,
            "walletCount", listUserWallets(userId).size()
        );
    }
}