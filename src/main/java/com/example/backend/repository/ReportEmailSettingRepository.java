package com.example.backend.repository;

import com.example.backend.entity.ReportEmailSetting;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportEmailSettingRepository extends JpaRepository<ReportEmailSetting, Long> {
    Optional<ReportEmailSetting> findByUser(User user);
}
