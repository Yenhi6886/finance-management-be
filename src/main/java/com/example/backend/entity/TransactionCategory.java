package com.example.backend.entity;

import com.example.backend.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "transactions_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Lob
    private String description;
    @Column(name = "budget", precision = 19, scale = 4)
    private BigDecimal budget; // Cho phép null nếu không có ngân sách

    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.VND;
}
