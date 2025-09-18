package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_email_settings")
@Getter
@Setter
public class ReportEmailSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_email")
    private String targetEmail;

    @Column(name = "daily_enabled")
    private boolean dailyEnabled;

    @Column(name = "weekly_enabled")
    private boolean weeklyEnabled;

    @Column(name = "monthly_enabled")
    private boolean monthlyEnabled;

    // 0-23
    @Column(name = "send_hour")
    private Integer sendHour;

    // 0-59
    @Column(name = "send_minute")
    private Integer sendMinute;

    // 1-7 (MONDAY=1 ... SUNDAY=7)
    @Column(name = "weekly_day_of_week")
    private Integer weeklyDayOfWeek;

    // 1-28/29/30/31 (we will clamp when generating period)
    @Column(name = "monthly_day_of_month")
    private Integer monthlyDayOfMonth;

    @Column(name = "last_daily_sent_at")
    private LocalDateTime lastDailySentAt;

    @Column(name = "last_weekly_sent_at")
    private LocalDateTime lastWeeklySentAt;

    @Column(name = "last_monthly_sent_at")
    private LocalDateTime lastMonthlySentAt;
}


