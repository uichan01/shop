package com.example.shop.cart.repository;

import com.example.shop.cart.domain.CartEntity;
import com.example.shop.cart.domain.CartItemEntity;
import com.example.shop.product.domain.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findAllByCart(CartEntity cart);

    Optional<CartItemEntity> findByCartAndProduct(CartEntity cart, ProductEntity product);
}
