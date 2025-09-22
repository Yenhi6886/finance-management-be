package com.example.backend.service;

import com.example.backend.dto.request.CategoryRequest;
import com.example.backend.dto.response.CategoryResponse;
import com.example.backend.entity.Category;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public CategoryResponse createCategory(CategoryRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .budgetAmount(request.getBudgetAmount())
                .budgetPeriod(request.getBudgetPeriod())
                .incomeTargetAmount(request.getIncomeTargetAmount())
                .incomeTargetPeriod(request.getIncomeTargetPeriod())
                .user(user)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return mapToCategoryResponse(savedCategory);
    }

    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));

        if (!category.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa danh mục này.");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setBudgetAmount(request.getBudgetAmount());
        category.setBudgetPeriod(request.getBudgetPeriod());
        category.setIncomeTargetAmount(request.getIncomeTargetAmount());
        category.setIncomeTargetPeriod(request.getIncomeTargetPeriod());

        Category updatedCategory = categoryRepository.save(category);
        return mapToCategoryResponse(updatedCategory);
    }

    public List<CategoryResponse> getCategories(Long userId) {
        List<Category> categories = categoryRepository.findByUserId(userId);
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCategory(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));

        if (!category.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa danh mục này.");
        }

        transactionRepository.setCategoryToNullByCategoryId(categoryId);
        categoryRepository.delete(category);
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX);

        // Luôn tính toán số tiền đã chi và đã thu
        BigDecimal spentAmount = transactionRepository.sumExpensesByCategoryIdAndDateRange(category.getId(), startOfMonth, endOfMonth);
        BigDecimal earnedAmount = transactionRepository.sumIncomesByCategoryIdAndDateRange(category.getId(), startOfMonth, endOfMonth);

        BigDecimal remainingAmount = null;
        // Chỉ tính số tiền còn lại nếu có đặt ngân sách
        if (category.getBudgetAmount() != null && category.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
            remainingAmount = category.getBudgetAmount().subtract(spentAmount);
        }

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .budgetAmount(category.getBudgetAmount())
                .budgetPeriod(category.getBudgetPeriod())
                .spentAmount(spentAmount)
                .remainingAmount(remainingAmount)
                .incomeTargetAmount(category.getIncomeTargetAmount())
                .incomeTargetPeriod(category.getIncomeTargetPeriod())
                .earnedAmount(earnedAmount)
                .build();
    }
}