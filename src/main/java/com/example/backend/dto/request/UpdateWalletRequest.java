package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateWalletRequest {

    @NotBlank(message = "{validation.notblank.wallet.name}")
    @Size(min = 2, max = 50, message = "{validation.size.wallet.name}")
    private String name;

    @NotBlank(message = "{validation.notblank.icon}")
    private String icon;

    @Size(max = 200, message = "{validation.size.description}")
    private String description;
}