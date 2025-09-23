package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "{validation.notblank.token}")
    private String token;

    @NotBlank(message = "{validation.notblank.newpassword}")
    @Size(min = 6, max = 8, message = "{validation.size.password}")
    private String newPassword;
}
