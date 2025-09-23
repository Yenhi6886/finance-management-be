package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "{validation.notblank.identifier}")
    private String identifier;

    @NotBlank(message = "{validation.notblank.password}")
    private String password;
}
