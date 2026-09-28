package com.ezbuy.ezbuy.services.impl;

import com.ezbuy.ezbuy.dtos.request.ProductRequest;
import com.ezbuy.ezbuy.dtos.response.PageResponse;
import com.ezbuy.ezbuy.dtos.response.ProductResponse;
import com.ezbuy.ezbuy.entities.Category;
import com.ezbuy.ezbuy.entities.Manufacturer;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.events.ProductDeactivatedEvent;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.mappers.ProductMapper;
import com.ezbuy.ezbuy.repositories.CategoryRepository;
import com.ezbuy.ezbuy.repositories.ManufacturerRepository;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import com.ezbuy.ezbuy.repositories.specifications.ProductSpecification;
import com.ezbuy.ezbuy.services.CategoryService;
import com.ezbuy.ezbuy.services.CloudinaryService;
import com.ezbuy.ezbuy.services.ProductService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;
    private final Slugify slugify = Slugify.builder().build();
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, MultipartFile file) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new NotFoundException("Manufacturer not found"));

        Product product = productMapper.toProduct(request);
        product.setCategory(category);
        product.setManufacturer(manufacturer);
        product.setSlug(generateUniqueActiveSlug(request.getName()));

        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(file, "products");
            product.setImageUrl(imageUrl);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
    public PageResponse<ProductResponse> getAllProducts(String keyword, Integer categoryId, Integer manufacturerId, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.empty();

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(ProductSpecification.hasKeyword(keyword));
        }

        if (categoryId != null) {
            List<Integer> allCategoryIds = categoryService.findSubtreeCategoryIds(categoryId);
            spec = spec.and(ProductSpecification.inCategories(allCategoryIds));
        }

        if (manufacturerId != null) {
            spec = spec.and(ProductSpecification.hasManufacturer(manufacturerId));
        }

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        
        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        return PageResponse.fromPage(productPage, productResponses);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product_detail", key = "#id", condition = "!T(com.ezbuy.ezbuy.utils.SecurityUtils).isAdmin()", unless = "#result == null")
    public Object getProductById(int id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        
        if (isAdmin) {
            return productMapper.toAdminProductDetailResponse(product);
        } else {
            return productMapper.toProductDetailResponse(product);
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product_detail", key = "#id"),
            @CacheEvict(value = "products_top_selling", allEntries = true)
    })
    public ProductResponse updateProduct(int id, ProductRequest request, MultipartFile file) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new NotFoundException("Manufacturer not found"));
        
        String originalName = existingProduct.getName();
        productMapper.updateProductFromRequest(existingProduct, request);

        if (!request.getName().equals(originalName)) {
            existingProduct.setSlug(generateUniqueActiveSlug(request.getName(), id));
        }


        if (file != null && !file.isEmpty()) {
            if (existingProduct.getImageUrl() != null && !existingProduct.getImageUrl().isEmpty()) {
                cloudinaryService.deleteImage(existingProduct.getImageUrl());
            }
            String newImageUrl = cloudinaryService.uploadFile(file, "products");
            existingProduct.setImageUrl(newImageUrl);
        }

        existingProduct.setCategory(category);
        existingProduct.setManufacturer(manufacturer);

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product_detail", key = "#id"),
            @CacheEvict(value = "products_top_selling", allEntries = true)
    })
    public void deleteProduct(int id) {
        Product productToDelete = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));

        
        productToDelete.setActive(false);
        productToDelete.setDeletedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

        productRepository.save(productToDelete);

        eventPublisher.publishEvent(new ProductDeactivatedEvent(this, productToDelete.getId()));
    }

    private String generateUniqueActiveSlug(String name) {
        return generateUniqueActiveSlug(name, null);
    }

    private String generateUniqueActiveSlug(String name, Integer currentId) {
        String baseSlug = slugify.slugify(name);
        String slug = baseSlug;
        int counter = 1;

        while ( (currentId == null && productRepository.existsBySlug(slug)) || 
                (currentId != null && productRepository.existsBySlugAndIdNot(slug, currentId)) ) 
        {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }
}