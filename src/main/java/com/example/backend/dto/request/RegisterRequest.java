package com.example.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "validation.notblank.email")
    @Email(message = "validation.email.invalid")
    private String email;

    @NotBlank(message = "validation.notblank.username")
    @Size(min = 3, max = 50, message = "validation.size.username")
    private String username;

    @NotBlank(message = "validation.notblank.password")
    @Size(min = 6, max = 8, message = "validation.size.password")
    private String password;

    @NotBlank(message = "validation.notblank.firstname")
    private String firstName;

    @NotBlank(message = "validation.notblank.lastname")
    private String lastName;

    private String phoneNumber;
}