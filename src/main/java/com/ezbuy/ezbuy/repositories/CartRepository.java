package com.ezbuy.ezbuy.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ezbuy.ezbuy.entities.Cart;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.entities.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    
    List<Cart> findByUser(User user);

    Optional<Cart> findByUserAndProduct(User user, Product product);

    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.product.id = :productId")
    void deleteByProductId(Integer productId);

    Page<Cart> findByUser(User user, Pageable pageable);

    Page<Cart> findByUserAndProduct_NameContainingIgnoreCase(User user, String productName, Pageable pageable);

    int countByUser(User user);

    @Modifying
    @Query(value = "DELETE FROM carts WHERE product_id = :productId", nativeQuery = true) 
    void deleteByProductIdNative(@Param("productId") Integer productId);
}