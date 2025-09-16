package com.example.backend.repository;

import com.example.backend.entity.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {
    // Find categories by name - now we use specific category names instead of types
    List<TransactionCategory> findByName(String categoryName);

    // Find categories by name pattern (for flexibility)
    List<TransactionCategory> findByNameContainingIgnoreCase(String namePattern);
}
