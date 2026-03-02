package com.ezbuy.ezbuy.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.ezbuy.ezbuy.repositories.CartRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartCleanupListener {

    private final CartRepository cartRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleProductDeactivation(ProductDeactivatedEvent event) {
        log.info("Product with ID {} was deactivated. Cleaning up carts...", event.getProductId());
        try {
            cartRepository.deleteByProductIdNative(event.getProductId()); 
            log.info("Finished cleaning carts for product ID {}.", event.getProductId());
        } catch (Exception e) {
            log.error("Error cleaning cart for product ID {}: {}", event.getProductId(), e.getMessage(), e);
        }
    }
}