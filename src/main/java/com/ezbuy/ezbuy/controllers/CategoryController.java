package com.ezbuy.ezbuy.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ezbuy.ezbuy.dtos.request.CategoryRequest;
import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.dtos.response.CategoryResponse;
import com.ezbuy.ezbuy.dtos.response.CategoryTreeResponse;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.services.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getAllCategories(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir 
    ) {
        List<String> allowedSortFields = List.of("id", "name", "createdAt");
        if (!allowedSortFields.contains(sortBy)) sortBy = "id";
        if (page < 0) page = 0;

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<CategoryResponse> categoriesPage = categoryService.getAllCategories(keyword, pageable);
        ApiResponse<PageResponse<CategoryResponse>> response = ApiResponse.success(categoriesPage, "Categories fetched successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getCategoryById(@PathVariable int id) {
        Object categoryData = categoryService.getCategoryById(id);
        ApiResponse<Object> response = ApiResponse.success(categoryData, "Category fetched successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getCategoryTree() {
        List<CategoryTreeResponse> categoryTree = categoryService.getCategoryTree();
        ApiResponse<List<CategoryTreeResponse>> response = ApiResponse.success(categoryTree, "Category tree fetched successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestPart("category") CategoryRequest request, 
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        CategoryResponse newCategory = categoryService.createCategory(request, file);
        ApiResponse<CategoryResponse> response = ApiResponse.success(newCategory, "Category created successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable int id,
            @Valid @RequestPart("category") CategoryRequest request, 
            @RequestPart(value = "file", required = false) MultipartFile file 
    ) {
        CategoryResponse updatedCategory = categoryService.updateCategory(id, request, file);
        ApiResponse<CategoryResponse> response = ApiResponse.success(updatedCategory, "Category updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable int id) {
        categoryService.deleteCategory(id);
        ApiResponse<Void> response = ApiResponse.success(null, "Category deleted successfully");
        return ResponseEntity.ok(response);
    }
}