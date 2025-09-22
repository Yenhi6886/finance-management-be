package com.example.backend.dto.response;

import com.example.backend.entity.WalletShare;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedWalletResponse {

    private Long id;
    private String name;
    private String icon;
    private String currency;
    private BigDecimal balance;
    private String description;
    private String ownerName;
    private String ownerEmail;
    private WalletShare.PermissionLevel permissionLevel;
    private LocalDateTime sharedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}