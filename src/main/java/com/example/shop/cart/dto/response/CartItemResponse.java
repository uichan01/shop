package com.example.shop.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class CartItemResponse {
    private Long productId;
    private String productName;
    private int productPrice;

    private int quantity;
    private int totalPrice;

    private String thumbnailUrl;
}
