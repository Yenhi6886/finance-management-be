package com.example.backend.dto.request;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String imageUrl;
}