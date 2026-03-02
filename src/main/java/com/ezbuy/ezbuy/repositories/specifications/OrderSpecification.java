package com.ezbuy.ezbuy.repositories.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.ezbuy.ezbuy.entities.Order;
import com.ezbuy.ezbuy.enums.OrderStatus;

public class OrderSpecification {
    public static Specification<Order> empty() {
        return (root, query, cb) -> cb.conjunction();
    }
    public static Specification<Order> hasId(Integer id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }
    public static Specification<Order> receiverNameContains(String receiverName) {
        return (root, query, cb) -> cb.like(root.get("receiverName"), "%" + receiverName + "%");
    }
    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}