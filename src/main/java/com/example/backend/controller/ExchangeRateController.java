package com.example.backend.controller;

import com.example.backend.dto.request.UpdateExchangeRateRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.ExchangeRateResponse;
import com.example.backend.enums.Currency;
import com.example.backend.service.ExchangeRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExchangeRateResponse>>> getAllExchangeRates() {
        List<ExchangeRateResponse> rates = exchangeRateService.getAllExchangeRates();
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách tỷ giá thành công", rates));
    }

    @GetMapping("/{currency}")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> getExchangeRateByCurrency(
            @PathVariable Currency currency) {
        ExchangeRateResponse rate = exchangeRateService.getExchangeRateByCurrency(currency);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy tỷ giá thành công", rate));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> updateExchangeRate(
            @Valid @RequestBody UpdateExchangeRateRequest request,
            Authentication authentication) {
        String updatedBy = authentication.getName();
        ExchangeRateResponse rate = exchangeRateService.updateExchangeRate(request, updatedBy);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật tỷ giá thành công", rate));
    }
}
