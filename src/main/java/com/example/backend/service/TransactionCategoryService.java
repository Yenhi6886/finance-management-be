package com.example.backend.service;

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
                        .build())
                .collect(Collectors.toList());
    }
}
