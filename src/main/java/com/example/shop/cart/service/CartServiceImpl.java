package com.example.shop.cart.service;

import com.example.shop.cart.dto.request.AddCartItemRequest;
import com.example.shop.cart.dto.request.UpdateCartItemRequest;
import com.example.shop.cart.dto.response.CartInfoResponse;

public class CartServiceImpl implements CartService{

    @Override
    public CartInfoResponse getMyCart(String username) {
        return null;
    }

    @Override
    public void addProductToMyCart(String username, AddCartItemRequest request) {

    }

    @Override
    public void removeProductFromMyCart(String username, Long productId) {

    }

    @Override
    public void updateMyCartProductQuantity(String username, UpdateCartItemRequest request) {

    }
}
