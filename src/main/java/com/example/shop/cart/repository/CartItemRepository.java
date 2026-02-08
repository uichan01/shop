package com.example.shop.cart.repository;

import com.example.shop.cart.domain.CartEntity;
import com.example.shop.cart.domain.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findAllByCart(CartEntity cart);
}
