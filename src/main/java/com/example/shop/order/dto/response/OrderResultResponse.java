package com.example.shop.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class OrderResultResponse {
    private Long id;

    private List<OrderItemResponse> orderItems;

    private String orderNumber;

    private String address;

    private int totalPrice;

    private LocalDateTime createdAt;
}
