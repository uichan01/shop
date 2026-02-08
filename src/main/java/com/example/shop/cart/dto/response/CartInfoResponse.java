package com.example.shop.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class CartInfoResponse {
    private List<CartItemResponse> items;

    private int totalItemTypeCount; //상품 종류 수

    private int totalQuantity; //총 상품 개수

    private int totalPrice;
}
