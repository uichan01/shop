package com.example.shop.order.dto.response;

import com.example.shop.order.domain.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class OrderItemResponse {

    private Long id;

    private String productName;

    private int unitPrice;

    private int quantity;

    private OrderStatus status;
}
