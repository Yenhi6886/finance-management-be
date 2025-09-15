package com.example.backend.dto.request;

import com.example.backend.enums.PermissionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionRequest {

    @NotNull(message = "Danh sách quyền không được để trống")
    private List<PermissionType> permissions;

    private String reason;
}
