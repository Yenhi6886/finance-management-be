package com.example.backend.controller;

import com.example.backend.dto.request.CategoryRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.CategoryResponse;
import com.example.backend.dto.response.TransactionResponse;
import com.example.backend.security.CustomUserDetails;
import com.example.backend.service.CategoryService;
import com.example.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final TransactionService transactionService;


    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        CategoryResponse category = categoryService.createCategory(request, currentUser.getId());
        ApiResponse<CategoryResponse> response = new ApiResponse<>(true, "Tạo danh mục thành công", category);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        CategoryResponse category = categoryService.updateCategory(id, request, currentUser.getId());
        ApiResponse<CategoryResponse> response = new ApiResponse<>(true, "Cập nhật danh mục thành công", category);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<CategoryResponse> categories = categoryService.getCategories(currentUser.getId());
        ApiResponse<List<CategoryResponse>> response = new ApiResponse<>(true, "Lấy danh sách danh mục thành công", categories);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        CategoryResponse category = categoryService.getCategoryById(id, currentUser.getId());
        ApiResponse<CategoryResponse> response = new ApiResponse<>(true, "Lấy thông tin chi tiết danh mục thành công", category);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        categoryService.deleteCategory(id, currentUser.getId());
        ApiResponse<Void> response = new ApiResponse<>(true, "Xóa danh mục thành công", null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByCategoryId(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByCategoryId(id, currentUser.getId());
        ApiResponse<List<TransactionResponse>> response = new ApiResponse<>(true, "Lấy giao dịch theo danh mục thành công", transactions);
        return ResponseEntity.ok(response);
    }
}