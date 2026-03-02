package com.ezbuy.ezbuy.repositories;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ezbuy.ezbuy.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    
    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAndLockByIds(List<Integer> ids);

    @Query("SELECT p " +
           "FROM OrderItem oi JOIN oi.product p JOIN oi.order o " + 
           "WHERE p.category.id IN :categoryIds AND p.isActive = true " +
           "AND o.status <> com.ezbuy.ezbuy.enums.OrderStatus.CANCELLED " + 
           "AND o.orderDate >= :salesStartDate " + 
           "GROUP BY p " +
           "ORDER BY SUM(oi.quantity) DESC") 
    List<Product> findTopSellingProductsInCategories(
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("salesStartDate") LocalDateTime salesStartDate,
            Pageable pageable 
    );

    @Query("SELECT p " +
           "FROM OrderItem oi JOIN oi.product p JOIN oi.order o " + 
           "WHERE p.isActive = true " +
           "AND o.status <> com.ezbuy.ezbuy.enums.OrderStatus.CANCELLED " + 
           "AND o.orderDate >= :salesStartDate " + 
           "GROUP BY p " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Product> findOverallTopSellingProducts(
            @Param("salesStartDate") LocalDateTime salesStartDate,
            Pageable pageable 
    );

    @Query("SELECT p FROM Product p " + 
           "WHERE p.category.id IN :categoryIds AND p.isActive = true " + 
           "ORDER BY p.createdAt DESC")
    List<Product> findNewestProductsInCategories(
            @Param("categoryIds") List<Integer> categoryIds,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    List<Product> findOverallNewestProducts(Pageable pageable);
}