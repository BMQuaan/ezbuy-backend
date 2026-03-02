package com.ezbuy.ezbuy.services;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.ezbuy.ezbuy.dtos.request.ProductRequest;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.dtos.response.ProductResponse;

public interface ProductService {
    ProductResponse createProduct(ProductRequest productRequest, MultipartFile file);
    PageResponse<ProductResponse> getAllProducts(String keyword, Integer categoryId, Integer manufacturerId, Pageable pageable);
    Object getProductById(int id);
    ProductResponse updateProduct(int id, ProductRequest productRequest, MultipartFile file);
    void deleteProduct(int id);
}