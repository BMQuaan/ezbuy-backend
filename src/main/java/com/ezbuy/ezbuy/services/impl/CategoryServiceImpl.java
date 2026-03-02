package com.ezbuy.ezbuy.services.impl;

import com.ezbuy.ezbuy.dtos.request.CategoryRequest;
import com.ezbuy.ezbuy.dtos.response.CategoryResponse;
import com.ezbuy.ezbuy.dtos.response.CategoryTreeResponse;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.entities.Category;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.mappers.CategoryMapper;
import com.ezbuy.ezbuy.repositories.CategoryRepository;
import com.ezbuy.ezbuy.services.CategoryService;
import com.ezbuy.ezbuy.services.CloudinaryService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final Slugify slugify = Slugify.builder().build();
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, MultipartFile file) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DataIntegrityViolationException("Category name '" + request.getName() + "' already exists for an active category.");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setActive(request.isActive());
        category.setSlug(generateUniqueActiveSlug(request.getName()));

        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(file, "categories");
            category.setImageUrl(imageUrl);
        }

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Override
    public Object getCategoryById(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ADMIN"));

        if (isAdmin) {
            return categoryMapper.toCategoryDetailResponse(category);
        } else {
            return categoryMapper.toCategoryResponse(category);
        }
    }

    @Override
    public PageResponse<CategoryResponse> getAllCategories(String keyword, Pageable pageable) {
        Page<Category> categoryPage;
        if (keyword == null || keyword.isEmpty()) {
            categoryPage = categoryRepository.findAll(pageable);
        } else {
            categoryPage = categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
        }

        List<CategoryResponse> categoryResponses = categoryPage.getContent().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
        
        return PageResponse.fromPage(categoryPage, categoryResponses);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(int id, CategoryRequest request, MultipartFile file) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        
        if (!request.getName().equals(existingCategory.getName())) {
            if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new DataIntegrityViolationException("Category name '" + request.getName() + "' already exists for another active category.");
            }
            existingCategory.setSlug(generateUniqueActiveSlug(request.getName(), id));
        }

        existingCategory.setName(request.getName());
        existingCategory.setActive(request.isActive());

        if (file != null && !file.isEmpty()) {
            if (existingCategory.getImageUrl() != null && !existingCategory.getImageUrl().isEmpty()) {
                cloudinaryService.deleteImage(existingCategory.getImageUrl());
            }
            String newImageUrl = cloudinaryService.uploadFile(file, "categories");
            existingCategory.setImageUrl(newImageUrl);
        }

        if (request.getParentId() != null) {
            if (!request.getParentId().equals(id)) { 
                Category parent = categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
            if (isDescendant(existingCategory, parent)) {
                 throw new IllegalArgumentException("Cannot set a descendant as a parent, this would create a cycle.");
            }
                existingCategory.setParent(parent);
            } else {
                 throw new IllegalArgumentException("A category cannot be its own parent.");
            }
        } else {
            existingCategory.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toCategoryResponse(updatedCategory);
    }


    @Override
    @Transactional
    public void deleteCategory(int id) {
        Category categoryToDelete = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        if (categoryRepository.existsByParentId(id)) {
            throw new IllegalStateException("Cannot delete category because it is a parent to other categories.");
        }
        if (categoryRepository.hasProducts(id)) {
            throw new IllegalStateException("Cannot delete category because it contains products.");
        }
        
        categoryToDelete.setActive(false);
        categoryToDelete.setDeletedAt(LocalDateTime.now()); 

        categoryRepository.save(categoryToDelete);
    }

     @Override
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findRootCategoriesWithChildren();

        return rootCategories.stream()
                .map(categoryMapper::toCategoryTreeResponse)
                .toList();
    }

    private boolean isDescendant(Category parent, Category potentialChild) {
        Category current = potentialChild.getParent();
        while (current != null) {
            if (current.getId().equals(parent.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    @Override
    public List<Integer> findSubtreeCategoryIds(Integer parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new NotFoundException("Category not found with id: " + parentId);
        }

        List<Category> allCategories = categoryRepository.findAll();

        Map<Integer, List<Category>> childrenByParentId = allCategories.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        List<Integer> ids = new ArrayList<>();
        ids.add(parentId); 
        
        findDescendantIdsFromMap(parentId, ids, childrenByParentId);
        
        return ids;
    }

    private void findDescendantIdsFromMap(Integer currentParentId, List<Integer> ids, Map<Integer, List<Category>> childrenByParentId) {
        List<Category> children = childrenByParentId.get(currentParentId);

        if (children == null || children.isEmpty()) {
            return; 
        }

        for (Category child : children) {
            ids.add(child.getId());
            findDescendantIdsFromMap(child.getId(), ids, childrenByParentId);
        }
    }

    private String generateUniqueActiveSlug(String name) {
        return generateUniqueActiveSlug(name, null); 
    }

    private String generateUniqueActiveSlug(String name, Integer currentId) {
        String baseSlug = slugify.slugify(name);
        String slug = baseSlug;
        int counter = 1;

        while ( (currentId == null && categoryRepository.existsBySlug(slug)) || 
                (currentId != null && categoryRepository.existsBySlugAndIdNot(slug, currentId)) ) 
        {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

}