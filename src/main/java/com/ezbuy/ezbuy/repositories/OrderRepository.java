package com.ezbuy.ezbuy.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ezbuy.ezbuy.entities.Order;
import com.ezbuy.ezbuy.entities.User;
import com.ezbuy.ezbuy.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {

    @Query("SELECT o FROM Order o WHERE o.user = :user " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:id IS NULL OR o.id = :id)")
    Page<Order> findOrdersForUser(
            @Param("user") User user, 
            @Param("id") Integer id, 
            @Param("status") OrderStatus status, 
            Pageable pageable
    );
    
    Optional<Order> findByIdAndUser(Integer id, User user);

    @Query("SELECT o FROM Order o " +
           "WHERE o.status = :status " +
           "AND o.orderDate < :expirationTime " +
           "AND (o.vnpTransactionNo IS NULL OR o.paymentStatus != 'PAID') " +
           "AND UPPER(o.payment.method) = 'VNPAY'")
    List<Order> findExpiredVnpayOrders(
            @Param("status") OrderStatus status, 
            @Param("expirationTime") LocalDateTime expirationTime
    );
}