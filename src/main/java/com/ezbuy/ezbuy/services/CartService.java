package com.ezbuy.ezbuy.services;

import com.ezbuy.ezbuy.dtos.request.CartRequest;
import com.ezbuy.ezbuy.dtos.response.CartDetailResponse;

public interface CartService {
    CartDetailResponse getCart();
    CartDetailResponse addToCart(CartRequest request);
    CartDetailResponse updateCart(CartRequest request);
    CartDetailResponse removeFromCart(int productId);
    CartDetailResponse clearCart();
}