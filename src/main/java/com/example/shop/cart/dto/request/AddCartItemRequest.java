package com.example.shop.cart.dto.request;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddCartItemRequest {
    private Long productId;
    private int quantity;
}
