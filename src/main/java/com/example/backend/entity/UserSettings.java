package com.example.backend.entity;

import com.example.backend.entity.User;
import com.example.backend.entity.Wallet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserSettings {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 10)
    private String language = "vi";

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_format")
    private CurrencyFormat currencyFormat = CurrencyFormat.dot_separator;

    @Enumerated(EnumType.STRING)
    @Column(name = "date_format")
    private DateFormat dateFormat = DateFormat.DD_MM_YYYY;

    @Column(name = "daily_report", nullable = false)
    private boolean dailyReport = false;

    @Column(name = "weekly_report", nullable = false)
    private boolean weeklyReport = false;

    @Column(name = "monthly_report", nullable = false)
    private boolean monthlyReport = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_wallet_id")
    private Wallet currentWallet;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CurrencyFormat {
        dot_separator, comma_separator
    }

    public enum DateFormat {
        DD_MM_YYYY("DD/MM/YYYY"),
        MM_DD_YYYY("MM/DD/YYYY"),
        YYYY_MM_DD("YYYY/MM/DD");

        private final String format;

        DateFormat(String format) {
            this.format = format;
        }

        public String getFormat() {
            return format;
        }
    }
}
