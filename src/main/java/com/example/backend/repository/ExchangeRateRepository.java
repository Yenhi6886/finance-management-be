package com.example.backend.repository;

import com.example.backend.entity.ExchangeRate;
import com.example.backend.enums.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByCurrency(Currency currency);

    boolean existsByCurrency(Currency currency);
}
