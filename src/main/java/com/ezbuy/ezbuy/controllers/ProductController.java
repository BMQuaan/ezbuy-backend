package com.ezbuy.ezbuy.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ezbuy.ezbuy.dtos.request.ProductRequest;
import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.dtos.response.ProductResponse;
import com.ezbuy.ezbuy.services.ProductService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @Valid @RequestPart("product") ProductRequest request
    ) {
        ProductResponse newProduct = productService.createProduct(request, file);
        return new ResponseEntity<>(ApiResponse.success(newProduct, "Product created successfully"), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer manufacturerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        List<String> ALLOWED_SORT_FIELDS = List.of("id", "name", "price", "createdAt", "updatedAt");
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "id"; 
        }

        if (page < 0) {
            page = 0;
        }

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PageResponse<ProductResponse> productPage = productService.getAllProducts(keyword, categoryId, manufacturerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(productPage, "Products fetched successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getProductById(@PathVariable int id) {
        Object productData = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(productData, "Product fetched successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable int id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @Valid @RequestPart("product") ProductRequest request 
    ) {
        ProductResponse updatedProduct = productService.updateProduct(id, request, file);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }
}