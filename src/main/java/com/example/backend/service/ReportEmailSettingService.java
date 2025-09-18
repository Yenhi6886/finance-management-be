package com.example.backend.service;

import com.example.backend.entity.ReportEmailSetting;
import com.example.backend.entity.User;
import com.example.backend.repository.ReportEmailSettingRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportEmailSettingService {

    private final ReportEmailSettingRepository settingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<ReportEmailSetting> getByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return settingRepository.findByUser(user);
    }

    @Transactional
    public ReportEmailSetting upsert(Long userId, ReportEmailSetting payload) {
        User user = userRepository.findById(userId).orElseThrow();
        ReportEmailSetting setting = settingRepository.findByUser(user).orElseGet(ReportEmailSetting::new);
        setting.setUser(user);
        setting.setTargetEmail(payload.getTargetEmail());
        setting.setDailyEnabled(payload.isDailyEnabled());
        setting.setWeeklyEnabled(payload.isWeeklyEnabled());
        setting.setMonthlyEnabled(payload.isMonthlyEnabled());
        setting.setSendHour(payload.getSendHour());
        setting.setSendMinute(payload.getSendMinute());
        setting.setWeeklyDayOfWeek(payload.getWeeklyDayOfWeek());
        setting.setMonthlyDayOfMonth(payload.getMonthlyDayOfMonth());
        return settingRepository.save(setting);
    }
}


