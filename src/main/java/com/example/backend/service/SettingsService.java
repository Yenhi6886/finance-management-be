package com.example.backend.service;

import com.example.backend.dto.request.UpdateSettingsRequest;
import com.example.backend.entity.User;
import com.example.backend.entity.UserSettings;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    public UserSettings getUserSettings(Long userId) {
        return userSettingsRepository.findById(userId).orElseGet(() -> createDefaultSettings(userId));
    }

    @Transactional
    public UserSettings updateUserSettings(Long userId, UpdateSettingsRequest request) {
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        if (request.getUsdToVndRate() != null && request.getUsdToVndRate().compareTo(BigDecimal.ZERO) > 0) {
            settings.setUsdToVndRate(request.getUsdToVndRate());
        }
        if (request.getCurrencyFormat() != null) {
            settings.setCurrencyFormat(request.getCurrencyFormat());
        }
        if (request.getDateFormat() != null) {
            settings.setDateFormat(request.getDateFormat());
        }

        return userSettingsRepository.save(settings);
    }

    private UserSettings createDefaultSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        UserSettings newSettings = UserSettings.builder()
                .user(user)
                .usdToVndRate(new BigDecimal("25400.00"))
                .currencyFormat(UserSettings.CurrencyFormat.dot_separator)
                .dateFormat(UserSettings.DateFormat.DD_MM_YYYY)
                .build();

        return userSettingsRepository.save(newSettings);
    }
}