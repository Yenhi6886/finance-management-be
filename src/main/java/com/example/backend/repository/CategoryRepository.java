package com.example.backend.repository;

import com.example.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(c.budgetAmount), 0) FROM Category c WHERE c.user.id = :userId")
    BigDecimal sumBudgetAmountByUserId(@Param("userId") Long userId);
}