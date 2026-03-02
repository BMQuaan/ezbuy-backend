package com.ezbuy.ezbuy.services.impl;

import com.ezbuy.ezbuy.entities.Cart;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.entities.User;
import com.ezbuy.ezbuy.repositories.CartRepository;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import com.ezbuy.ezbuy.services.CartCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartCleanupServiceImpl implements CartCleanupService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) 
    public List<Cart> cleanCartAndGetValidItems(User user) {
        List<Cart> originalCartItems = cartRepository.findByUser(user);
        if (originalCartItems.isEmpty()) {
            return new ArrayList<>();
        }

        List<Integer> productIdsInCart = originalCartItems.stream()
                .filter(cartItem -> cartItem.getProduct() != null)
                .map(cartItem -> cartItem.getProduct().getId())
                .distinct() 
                .collect(Collectors.toList());

        Map<Integer, Product> productsMap = productRepository.findAllById(productIdsInCart).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Cart> validItems = new ArrayList<>();
        List<Cart> itemsToDelete = new ArrayList<>();

        for (Cart item : originalCartItems) {
            Product originalProduct = item.getProduct();
            if (originalProduct == null) {
                itemsToDelete.add(item);
                continue;
            }
            
            Product product = productsMap.get(originalProduct.getId());
            if (product == null || !product.isActive() || product.getQuantityInStock() < item.getQuantity()) {
                itemsToDelete.add(item);
            } else {
                validItems.add(item);
            }
        }

        if (!itemsToDelete.isEmpty()) {
            cartRepository.deleteAll(itemsToDelete);
        }
        
        return validItems;
    }
}