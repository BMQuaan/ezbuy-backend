package com.ezbuy.ezbuy.repositories.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.ezbuy.ezbuy.entities.Promotion;

public class PromotionSpecification {

    public static Specification<Promotion> empty() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Promotion> codeContains(String code) {
        return (root, query, cb) -> 
            cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%");
    }
}