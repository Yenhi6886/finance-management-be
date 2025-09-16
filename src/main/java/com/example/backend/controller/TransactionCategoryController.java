package com.example.backend.controller;

import com.example.backend.dto.request.TransactionCategoryCreateRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.TransactionCategoryResponse;
import com.example.backend.service.TransactionCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class TransactionCategoryController {

    private final TransactionCategoryService categoryService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<TransactionCategoryResponse>>> getAllCategories() {
        List<TransactionCategoryResponse> categories = categoryService.getAllCategories();
        ApiResponse<List<TransactionCategoryResponse>> response = new ApiResponse<>(true, "Lấy danh sách danh mục thành công", categories);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TransactionCategoryResponse>> createCategory(@Valid @RequestBody TransactionCategoryCreateRequest request) {
        TransactionCategoryResponse category = categoryService.createCategory( request );
        ApiResponse<TransactionCategoryResponse> response = new ApiResponse<>(true, "Tạo danh mục thành công", category);
        return ResponseEntity.ok(response);
    }
}
