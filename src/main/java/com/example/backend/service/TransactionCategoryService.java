package com.example.backend.service;

import com.example.backend.dto.request.TransactionCategoryCreateRequest;
import com.example.backend.dto.response.TransactionCategoryResponse;
import com.example.backend.entity.TransactionCategory;
import com.example.backend.repository.TransactionCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionCategoryService {

    private final TransactionCategoryRepository categoryRepository;


    public List<TransactionCategoryResponse> getAllCategories() {
        List<TransactionCategory> categories = categoryRepository.findAll();
        return mapCategoriesToResponse(categories);
    }

    private List<TransactionCategoryResponse> mapCategoriesToResponse(List<TransactionCategory> categories) {
        return categories.stream()
                .map(category -> TransactionCategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .budget(category.getBudget())
                        .currency(category.getCurrency())
                        .build())
                .collect(Collectors.toList());
    }

    public TransactionCategoryResponse createCategory(TransactionCategoryCreateRequest request) {
        // Debug logging để xem giá trị thực tế
        System.out.println("Debug - Name: " + request.getName());
        System.out.println("Debug - Description: " + request.getDescription());
        System.out.println("Debug - Budget: " + request.getBudget());
        System.out.println("Debug - Currency: " + request.getCurrency());

        TransactionCategory category = TransactionCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .budget(request.getBudget())
                .currency(request.getCurrency())
                .build();

        TransactionCategory savedCategory = categoryRepository.save(category);

        return TransactionCategoryResponse.builder()
                .id(savedCategory.getId())
                .name(savedCategory.getName())
                .description(savedCategory.getDescription())
                .budget(savedCategory.getBudget())
                .currency(savedCategory.getCurrency()) // Thêm currency vào response
                .build();
    }
}
