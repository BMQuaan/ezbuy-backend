package com.ezbuy.ezbuy.services;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.ezbuy.ezbuy.dtos.request.CategoryRequest;
import com.ezbuy.ezbuy.dtos.response.CategoryResponse;
import com.ezbuy.ezbuy.dtos.response.CategoryTreeResponse;
import com.ezbuy.ezbuy.dtos.response.PageResponse;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest categoryRequest, MultipartFile file);
    Object getCategoryById(int id);
    PageResponse<CategoryResponse> getAllCategories(String keyword, Pageable pageable);
    CategoryResponse updateCategory(int id, CategoryRequest categoryRequest, MultipartFile file);
    void deleteCategory(int id);
    List<CategoryTreeResponse> getCategoryTree();
    List<Integer> findSubtreeCategoryIds(Integer parentId);
}