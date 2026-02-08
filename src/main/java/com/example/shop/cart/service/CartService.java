package com.example.shop.cart.service;

import com.example.shop.cart.dto.request.AddCartItemRequest;
import com.example.shop.cart.dto.request.UpdateCartItemRequest;
import com.example.shop.cart.dto.response.CartInfoResponse;

public interface CartService {
    //본인 장바구니조회-유효성검증+총가격요약
    public CartInfoResponse getMyCart(String username);
    //장바구니 상품추가
    public void addProductToMyCart(String username, AddCartItemRequest request);
    //장바구니 상품삭제
    public void removeProductFromMyCart(String username, Long productId);
    //장바구니 수량변경
    public void updateMyCartProductQuantity(String username, UpdateCartItemRequest request);
}
