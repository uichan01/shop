package com.example.shop.order.service;

import com.example.shop.order.domain.OrderStatus;
import com.example.shop.order.dto.response.OrderResultResponse;

import java.util.List;

public interface OrderService {
    OrderResultResponse createOrder(String email, String address);
    OrderResultResponse getOrder(String email, Long orderId);
    List<OrderResultResponse> getMyOrders(String email);
    void cancelOrderItem(String email, Long orderId, Long orderItemId);
    void updateOrderItemStatus(String email, Long orderItemId, OrderStatus status);
}
