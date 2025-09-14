package com.example.backend.repository;

import com.example.backend.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    List<UserSettings> findByCurrentWalletId(Long walletId);
}