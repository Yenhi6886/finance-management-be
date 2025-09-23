package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class LocaleTestController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/locale")
    public ApiResponse<Map<String, Object>> testLocale() {
        Map<String, Object> data = new HashMap<>();
        
        // Current locale
        data.put("currentLocale", LocaleContextHolder.getLocale().toString());
        
        // Test messages
        data.put("validationEmail", messageService.getMessage("validation.notblank.email"));
        data.put("authBadCredentials", messageService.getMessage("auth.bad.credentials"));
        data.put("exceptionNotFound", messageService.getMessage("exception.resource.not.found"));
        data.put("success", messageService.getMessage("success"));
        
        // Test parameterized message
        data.put("walletShareSubject", messageService.getMessage("wallet.share.invitation.subject", 
            new Object[]{"John Doe", "My Wallet"}));
        
        return new ApiResponse<>(true, messageService.getMessage("success"), data);
    }

    @GetMapping("/validation-test")
    public ApiResponse<String> testValidationMessage() {
        // This will demonstrate validation message localization
        return new ApiResponse<>(true, messageService.getMessage("success"), 
            messageService.getMessage("validation.notblank.email"));
    }
}
