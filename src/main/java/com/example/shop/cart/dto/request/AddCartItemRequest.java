package com.example.shop.cart.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class AddCartItemRequest {
    private Long productId;
    private int quantity;
}
