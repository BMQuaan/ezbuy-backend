package com.ezbuy.ezbuy.mappers;

import org.mapstruct.AfterMapping; 
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget; 
import org.mapstruct.Named;

import com.ezbuy.ezbuy.dtos.response.AdminOrderDetailResponse;
import com.ezbuy.ezbuy.dtos.response.AdminOrderSummaryResponse;
import com.ezbuy.ezbuy.dtos.response.OrderDetailResponse;
import com.ezbuy.ezbuy.dtos.response.OrderItemResponse;
import com.ezbuy.ezbuy.dtos.response.OrderResponse;
import com.ezbuy.ezbuy.dtos.response.OrderSummaryResponse;
import com.ezbuy.ezbuy.entities.Order;
import com.ezbuy.ezbuy.entities.OrderItem;
import com.ezbuy.ezbuy.entities.User;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @Mapping(source = "id", target = "orderId")
    @Mapping(source = "orderItems", target = "items")
    OrderResponse toOrderResponse(Order order);

    @AfterMapping
    default void setImageUrlIfProductExists(@MappingTarget OrderItemResponse response, OrderItem orderItem) {
        if (orderItem.getProduct() != null) {
            response.setProductImageUrl(orderItem.getProduct().getImageUrl());
        }
    }

    @Mapping(source = "payment.method", target = "paymentMethod")
    OrderSummaryResponse toOrderSummaryResponse(Order order);
    
    @Mapping(source = "user.email", target = "userEmail")
    AdminOrderSummaryResponse toAdminOrderSummaryResponse(Order order);

    @Mapping(source = "payment.method", target = "paymentMethod")
    @Mapping(source = "orderItems", target = "items")
    OrderDetailResponse toOrderDetailResponse(Order order);

    @Mapping(source = "payment.method", target = "paymentMethod")
    @Mapping(source = "orderItems", target = "items")
    @Mapping(source = "user", target = "userFullName", qualifiedByName = "userToFullName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "confirmedBy", target = "confirmedByName", qualifiedByName = "userToFullName")
    AdminOrderDetailResponse toAdminOrderDetailResponse(Order order);
    
    @Named("userToFullName")
    default String userToFullName(User user) {
        if (user == null) return null;
        return user.getFirstName() + " " + user.getLastName();
    }
}