package com.example.backend.dto.response;

import com.example.backend.enums.AuthProvider;
import com.example.backend.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String avatarUrl;
    private UserStatus status;
    private AuthProvider authProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}