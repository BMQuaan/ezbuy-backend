package com.ezbuy.ezbuy.services.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezbuy.ezbuy.dtos.request.CartRequest;
import com.ezbuy.ezbuy.dtos.response.CartDetailResponse;
import com.ezbuy.ezbuy.dtos.response.CartResponse;
import com.ezbuy.ezbuy.entities.Cart;
import com.ezbuy.ezbuy.entities.Product;
import com.ezbuy.ezbuy.entities.User;
import com.ezbuy.ezbuy.exceptions.NotFoundException;
import com.ezbuy.ezbuy.mappers.CartMapper;
import com.ezbuy.ezbuy.repositories.CartRepository;
import com.ezbuy.ezbuy.repositories.ProductRepository;
import com.ezbuy.ezbuy.services.CartService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    @Transactional 
    public CartDetailResponse getCart() {
        User currentUser = getCurrentUser();
        return getCurrentCartDetails(currentUser);
    }


    @Override
    @Transactional
    public CartDetailResponse addToCart(CartRequest request) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (product.getQuantityInStock() < request.getQuantity()) {
            throw new IllegalArgumentException("Not enough products in stock");
        }

        Optional<Cart> existingCartItem = cartRepository.findByUserAndProduct(currentUser, product);
        
        if (existingCartItem.isPresent()) {
            Cart cart = existingCartItem.get();
            int newQuantity = cart.getQuantity() + request.getQuantity();
            if (product.getQuantityInStock() < newQuantity) {
                throw new IllegalArgumentException("Not enough products in stock for the new quantity");
            }
            cart.setQuantity(newQuantity);
            cartRepository.save(cart);
        } else {
            Cart newCartItem = Cart.builder()
                    .user(currentUser)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartRepository.save(newCartItem);
        }
        
        return getCurrentCartDetails(currentUser);
    }

    @Override
    @Transactional
    public CartDetailResponse updateCart(CartRequest request) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Cart cartItem = cartRepository.findByUserAndProduct(currentUser, product)
                .orElseThrow(() -> new NotFoundException("Product not found in cart"));

        if (product.getQuantityInStock() < request.getQuantity()) {
            throw new IllegalArgumentException("Not enough products in stock");
        }

        cartItem.setQuantity(request.getQuantity());
        cartRepository.save(cartItem);
        
        return getCurrentCartDetails(currentUser);
    }

    @Override
    @Transactional
    public CartDetailResponse removeFromCart(int productId) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        
        Cart cartItem = cartRepository.findByUserAndProduct(currentUser, product)
                .orElseThrow(() -> new NotFoundException("Product not found in cart"));

        cartRepository.delete(cartItem);
        
        return getCurrentCartDetails(currentUser);
    }

    @Override
    @Transactional
    public CartDetailResponse clearCart() {
        User currentUser = getCurrentUser();
        cartRepository.deleteByUser(currentUser);
        
        return getCurrentCartDetails(currentUser);
    }

    private CartDetailResponse getCurrentCartDetails(User user) {
        List<Cart> cartItems = cartRepository.findByUser(user);

        List<CartResponse> validItems = cartItems.stream()
                .filter(item -> item.getProduct() != null)
                .map(cartMapper::toCartResponse)
                .collect(Collectors.toList());

        BigDecimal totalCartPrice = validItems.stream()
                .map(CartResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDetailResponse.builder()
                .items(validItems)
                .totalCartPrice(totalCartPrice)
                .build();
    }
}