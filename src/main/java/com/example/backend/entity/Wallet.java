package com.example.backend.entity;

import com.example.backend.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "wallets",
    indexes = @Index(name = "idx_user_id", columnList = "user_id")
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String icon;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal balance = new BigDecimal("0.00");

    @Column(name = "currency_code", length = 10, nullable = false)
    private String currencyCode = "VND";

    @Lob
    private String description;

    @Column(name = "is_archived", nullable = false)
    private boolean archived = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}