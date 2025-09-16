package com.example.backend.dto.response;

import com.example.backend.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWalletPermissionsResponse {

    private Long walletId;
    private String walletName;
    private String ownerName;
    private String ownerEmail;
    private List<PermissionType> permissions;
    private List<String> permissionDisplayNames;
    private LocalDateTime sharedAt;
    private Boolean isActive;
}
