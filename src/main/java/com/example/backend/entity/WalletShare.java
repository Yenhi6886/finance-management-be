package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_shares")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "shared_with_user_id", nullable = false)
    private Long sharedWithUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_level", nullable = false)
    private PermissionLevel permissionLevel = PermissionLevel.VIEWER;

    @Column(name = "shared_with_email", nullable = false)
    private String sharedWithEmail;

    @Column(name = "share_token", unique = true)
    private String shareToken;

    @Column(name = "share_method")
    private String shareMethod;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Quan hệ liên kết
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id", insertable = false, updatable = false)
    private User sharedWithUser;

    public enum PermissionLevel {
        VIEWER,    // Chỉ có thể xem
        EDITOR,    // Có thể chỉnh sửa
        OWNER      // Quyền hoàn toàn
    }
}
