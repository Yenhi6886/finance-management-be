package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "{validation.notblank.currentpassword}")
    private String currentPassword;

    @NotBlank(message = "{validation.notblank.newpassword}")
    @Size(min = 6, max = 8, message = "{validation.size.password}")
    private String newPassword;
}
