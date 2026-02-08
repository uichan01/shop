package com.example.shop.cart.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class UpdateCartItemRequest {
    private Long productId;
    private int quantity;
}
