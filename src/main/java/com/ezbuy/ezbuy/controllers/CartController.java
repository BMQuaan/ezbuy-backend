package com.ezbuy.ezbuy.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ezbuy.ezbuy.dtos.request.CartRequest;
import com.ezbuy.ezbuy.dtos.response.ApiResponse;
import com.ezbuy.ezbuy.dtos.response.CartDetailResponse; 
import com.ezbuy.ezbuy.services.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDetailResponse>> getCart() { 
        CartDetailResponse cartDetail = cartService.getCart();
        return ResponseEntity.ok(ApiResponse.success(cartDetail, "Cart fetched successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartDetailResponse>> addToCart(@Valid @RequestBody CartRequest request) { 
        CartDetailResponse cartDetail = cartService.addToCart(request);
        return ResponseEntity.ok(ApiResponse.success(cartDetail, "Product added to cart successfully"));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CartDetailResponse>> updateCart(@Valid @RequestBody CartRequest request) { 
        CartDetailResponse cartDetail = cartService.updateCart(request);
        return ResponseEntity.ok(ApiResponse.success(cartDetail, "Cart updated successfully"));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<CartDetailResponse>> removeFromCart(@PathVariable int productId) {
        CartDetailResponse cartDetail = cartService.removeFromCart(productId);
        return ResponseEntity.ok(ApiResponse.success(cartDetail, "Product removed from cart successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<CartDetailResponse>> clearCart() { 
        CartDetailResponse cartDetail = cartService.clearCart();
        return ResponseEntity.ok(ApiResponse.success(cartDetail, "Cart cleared successfully"));
    }
}