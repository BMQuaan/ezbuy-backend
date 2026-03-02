package com.ezbuy.ezbuy.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.ezbuy.ezbuy.dtos.response.CartResponse;
import com.ezbuy.ezbuy.entities.Cart;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "id", target = "cartId")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.imageUrl", target = "productImageUrl")
    @Mapping(source = "product.price", target = "price")
    CartResponse toCartResponse(Cart cart);

    @AfterMapping
    default void calculateTotalPrice(@MappingTarget CartResponse cartResponse, Cart cart) {
        if (cart.getProduct() != null && cart.getProduct().getPrice() != null) {
            BigDecimal totalPrice = cart.getProduct().getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            cartResponse.setTotalPrice(totalPrice);
        }
    }
}