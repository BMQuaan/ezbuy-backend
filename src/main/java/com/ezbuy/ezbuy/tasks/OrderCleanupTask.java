package com.ezbuy.ezbuy.tasks;

import com.ezbuy.ezbuy.entities.Order;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.enums.OrderStatus;
import com.ezbuy.ezbuy.repositories.OrderRepository;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCleanupTask {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Scheduled(fixedRate = 300000) 
    @Transactional
    public void cancelUnpaidOrders() {
        log.info("Starting job to scan for expired unpaid orders...");

        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);

        List<Order> expiredOrders = orderRepository.findExpiredVnpayOrders(
                OrderStatus.PENDING, 
                expirationTime
        );

        for (Order order : expiredOrders) {
            try {
                order.setStatus(OrderStatus.CANCELLED);
                order.setNote("Automatic cancellation: VNPay payment is overdue and no transaction code has been issued.");

                order.getOrderItems().forEach(item -> {
                    Product product = item.getProduct();
                    int newStock = product.getQuantityInStock() + item.getQuantity();
                    product.setQuantityInStock(newStock);
                    
                    productRepository.save(product);
                });

                orderRepository.save(order);
                
                log.info("Order ID: {} has been cancelled.", order.getId());

            } catch (Exception e) {
                log.error("Error processing order cancellation ID: " + order.getId(), e);
            }
        }
        log.info("Job finished. Processed {} orders.", expiredOrders.size());
    }
}