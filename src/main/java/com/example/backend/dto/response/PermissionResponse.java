package com.example.backend.dto.response;

import com.example.backend.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {

    private Long id;
    private Long walletShareId;
    private PermissionType permissionType;
    private String permissionDisplayName;
    private Boolean isGranted;
    private Long grantedBy;
    private LocalDateTime grantedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
