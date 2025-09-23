package com.example.backend.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải đúng 10 chữ số")
    private String phoneNumber;
    private String avatarUrl;


}