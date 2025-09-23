package com.example.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "{validation.notblank.email}")
    @Email(message = "{validation.email.invalid}")
    private String email;
}
