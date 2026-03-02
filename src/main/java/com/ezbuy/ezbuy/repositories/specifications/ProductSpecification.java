package com.ezbuy.ezbuy.repositories.specifications;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.ezbuy.ezbuy.entities.Product;

public class ProductSpecification {

    public static Specification<Product> empty() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    public static Specification<Product> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), 
                    "%" + keyword.toLowerCase() + "%"       
            );
        };
    }

    // public static Specification<Product> hasCategory(Integer categoryId) {
    //     return (root, query, criteriaBuilder) -> 
    //             criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    // }

     public static Specification<Product> inCategories(List<Integer> categoryIds) {
        return (root, query, criteriaBuilder) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return criteriaBuilder.conjunction(); 
            }
            return root.get("category").get("id").in(categoryIds);
        };
    }

    public static Specification<Product> hasManufacturer(Integer manufacturerId) {
        return (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("manufacturer").get("id"), manufacturerId);
    }
}