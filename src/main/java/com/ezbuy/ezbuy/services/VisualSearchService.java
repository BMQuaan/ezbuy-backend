package com.ezbuy.ezbuy.services;

import com.ezbuy.ezbuy.dtos.response.ProductResponse;
import com.ezbuy.ezbuy.dtos.response.VisualSearchResponse;
import com.ezbuy.ezbuy.entities.Category;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.mappers.ProductMapper;
import com.ezbuy.ezbuy.repositories.CategoryRepository;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisualSearchService {

    private final RestTemplate restTemplate;
    private final CategoryService categoryService; 
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Value("${ai.classification.service.url}") 
    private String classificationApiUrl;

    private static final Logger log = LoggerFactory.getLogger(VisualSearchService.class);

    public VisualSearchResponse searchByImage(MultipartFile file) {
        String predictedCategoryName = null;
        List<Integer> categoryIds = Collections.emptyList(); 
        boolean aiCallSuccessful = false;

        try {
            predictedCategoryName = callClassificationApi(file);
            Category predictedCategory = categoryRepository.findByName(predictedCategoryName).orElse(null);

            if (predictedCategory != null) {
                categoryIds = categoryService.findSubtreeCategoryIds(predictedCategory.getId());
                aiCallSuccessful = true;
            } else {
                log.warn("Predicted category '{}' not found in database. Proceeding with fallback.", predictedCategoryName);
            }

        } catch (Exception e) {
            log.error("Failed to get prediction from AI service or find category. Proceeding with fallback.", e);
        }

        List<Product> finalProductList = new ArrayList<>();

        if (aiCallSuccessful && !categoryIds.isEmpty()) {
            log.info("Finding products in categories: {}", categoryIds);
            
            Pageable top3SellingPage = PageRequest.of(0, 3);
            LocalDateTime salesStartDate = LocalDateTime.now().minusMonths(6);
            List<Product> topSellingUnsorted = productRepository.findTopSellingProductsInCategories(categoryIds, salesStartDate, top3SellingPage);

            List<Product> topSellingSortedByDate = topSellingUnsorted.stream()
                    .sorted(Comparator.comparing(Product::getCreatedAt).reversed()) 
                    .collect(Collectors.toList());

            Pageable top2NewestPage = PageRequest.of(0, 2);
            List<Product> newestInCategory = productRepository.findNewestProductsInCategories(categoryIds, top2NewestPage);

            Set<Product> combinedSet = new LinkedHashSet<>(topSellingSortedByDate); 
            combinedSet.addAll(newestInCategory);
            
            finalProductList = new ArrayList<>(combinedSet).stream().limit(5).toList();

            if (finalProductList.isEmpty()) {
                log.info("No products found in predicted categories. Executing fallback.");
                predictedCategoryName = null; 
            }
        }

        if (predictedCategoryName == null) {
            log.info("Executing fallback: combining top 3 overall best-selling (sorted by date) and top 2 overall newest.");
                
            Pageable top3OverallSellingPage = PageRequest.of(0, 3);
            LocalDateTime salesStartDate = LocalDateTime.now().minusMonths(6);
            List<Product> topSellingOverallUnsorted = productRepository.findOverallTopSellingProducts(salesStartDate, top3OverallSellingPage);

            List<Product> topSellingOverallSortedByDate = topSellingOverallUnsorted.stream()
                    .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                    .collect(Collectors.toList());

            Pageable top2OverallNewestPage = PageRequest.of(0, 2);
            List<Product> newestOverall = productRepository.findOverallNewestProducts(top2OverallNewestPage);

            Set<Product> combinedFallbackSet = new LinkedHashSet<>(topSellingOverallSortedByDate);
            combinedFallbackSet.addAll(newestOverall);
            
            finalProductList = new ArrayList<>(combinedFallbackSet).stream().limit(5).toList();
        }

        List<ProductResponse> productResponses = finalProductList.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());

        return VisualSearchResponse.builder()
                .predictedCategoryName(predictedCategoryName) 
                .products(productResponses)
                .build();
    }

    private String callClassificationApi(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        body.add("file", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response; 
        try {
            response = restTemplate.exchange(
                    classificationApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {} 
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to AI service: " + e.getMessage());
        }
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Object categoryObj = response.getBody().get("predicted_category");
            if (categoryObj instanceof String category) { 
                return category;
            } else {
                 throw new RuntimeException("AI service returned an invalid response format for 'predicted_category'.");
            }
        } else {
            throw new RuntimeException("Failed to get prediction from AI service. Status: " + response.getStatusCode());
        }
    }
}