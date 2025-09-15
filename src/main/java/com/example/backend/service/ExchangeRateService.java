package com.example.backend.service;

import com.example.backend.dto.request.UpdateExchangeRateRequest;
import com.example.backend.dto.response.ExchangeRateResponse;
import com.example.backend.entity.ExchangeRate;
import com.example.backend.enums.Currency;
import com.example.backend.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    @PostConstruct
    public void initializeExchangeRates() {
        // Khởi tạo tỷ giá mặc định nếu chưa có trong database
        if (exchangeRateRepository.count() == 0) {
            // VND là base currency (1 VND = 1 VND)
            createOrUpdateExchangeRate(Currency.VND, BigDecimal.ONE, "SYSTEM");
            // USD: 1 USD = 24,500 VND
            createOrUpdateExchangeRate(Currency.USD, new BigDecimal("24500"), "SYSTEM");
            // EUR: 1 EUR = 26,800 VND
            createOrUpdateExchangeRate(Currency.EUR, new BigDecimal("26800"), "SYSTEM");
            // JPY: 1 JPY = 165 VND
            createOrUpdateExchangeRate(Currency.JPY, new BigDecimal("165"), "SYSTEM");
            // GBP: 1 GBP = 30,200 VND
            createOrUpdateExchangeRate(Currency.GBP, new BigDecimal("30200"), "SYSTEM");
        }
    }

    /**
     * Chuyển đổi số tiền từ một loại tiền tệ sang loại khác
     * @param amount Số tiền cần chuyển đổi
     * @param fromCurrency Loại tiền tệ nguồn
     * @param toCurrency Loại tiền tệ đích
     * @return Số tiền sau khi chuyển đổi
     */
    public BigDecimal convertCurrency(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        BigDecimal fromRate = getExchangeRateFromDB(fromCurrency);
        BigDecimal toRate = getExchangeRateFromDB(toCurrency);

        if (fromRate == null || toRate == null) {
            throw new IllegalArgumentException("Không hỗ trợ loại tiền tệ: " + fromCurrency + " hoặc " + toCurrency);
        }

        // Chuyển về VND trước, sau đó chuyển sang tiền tệ đích
        BigDecimal vndAmount = amount.multiply(fromRate);
        BigDecimal convertedAmount = vndAmount.divide(toRate, 8, RoundingMode.HALF_UP);

        return convertedAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Chuyển đổi số tiền về USD để so sánh
     * @param amount Số tiền
     * @param currency Loại tiền tệ
     * @return Số tiền tương đương USD
     */
    public BigDecimal convertToUSD(BigDecimal amount, Currency currency) {
        return convertCurrency(amount, currency, Currency.USD);
    }

    /**
     * Kiểm tra xem loại tiền tệ có được hỗ trợ không
     * @param currency Loại tiền tệ cần kiểm tra
     * @return true nếu được hỗ trợ
     */
    public boolean isSupportedCurrency(Currency currency) {
        return exchangeRateRepository.existsByCurrency(currency);
    }

    /**
     * Lấy tỷ giá của một loại tiền tệ so với VND
     * @param currency Loại tiền tệ
     * @return Tỷ giá so với VND
     */
    public BigDecimal getExchangeRate(Currency currency) {
        return getExchangeRateFromDB(currency);
    }

    private BigDecimal getExchangeRateFromDB(Currency currency) {
        return exchangeRateRepository.findByCurrency(currency)
                .map(ExchangeRate::getRateToVND)
                .orElse(null);
    }

    /**
     * Cập nhật tỷ giá
     */
    @Transactional
    public ExchangeRateResponse updateExchangeRate(UpdateExchangeRateRequest request, String updatedBy) {
        ExchangeRate exchangeRate = exchangeRateRepository.findByCurrency(request.getCurrency())
                .orElse(ExchangeRate.builder()
                        .currency(request.getCurrency())
                        .build());

        exchangeRate.setRateToVND(request.getRateToVND());
        exchangeRate.setLastUpdated(LocalDateTime.now());
        exchangeRate.setUpdatedBy(updatedBy);

        ExchangeRate saved = exchangeRateRepository.save(exchangeRate);

        return ExchangeRateResponse.builder()
                .id(saved.getId())
                .currency(saved.getCurrency())
                .rateToVND(saved.getRateToVND())
                .lastUpdated(saved.getLastUpdated())
                .updatedBy(saved.getUpdatedBy())
                .build();
    }

    /**
     * Lấy danh sách tất cả tỷ giá
     */
    public List<ExchangeRateResponse> getAllExchangeRates() {
        return exchangeRateRepository.findAll().stream()
                .map(rate -> ExchangeRateResponse.builder()
                        .id(rate.getId())
                        .currency(rate.getCurrency())
                        .rateToVND(rate.getRateToVND())
                        .lastUpdated(rate.getLastUpdated())
                        .updatedBy(rate.getUpdatedBy())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Lấy tỷ giá của một loại tiền tệ cụ thể
     */
    public ExchangeRateResponse getExchangeRateByCurrency(Currency currency) {
        ExchangeRate rate = exchangeRateRepository.findByCurrency(currency)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tỷ giá cho " + currency));

        return ExchangeRateResponse.builder()
                .id(rate.getId())
                .currency(rate.getCurrency())
                .rateToVND(rate.getRateToVND())
                .lastUpdated(rate.getLastUpdated())
                .updatedBy(rate.getUpdatedBy())
                .build();
    }

    private void createOrUpdateExchangeRate(Currency currency, BigDecimal rate, String updatedBy) {
        ExchangeRate exchangeRate = exchangeRateRepository.findByCurrency(currency)
                .orElse(ExchangeRate.builder()
                        .currency(currency)
                        .build());

        exchangeRate.setRateToVND(rate);
        exchangeRate.setLastUpdated(LocalDateTime.now());
        exchangeRate.setUpdatedBy(updatedBy);

        exchangeRateRepository.save(exchangeRate);
    }
}
