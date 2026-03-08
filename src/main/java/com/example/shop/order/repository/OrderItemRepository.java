package com.example.shop.order.repository;

import com.example.shop.order.domain.OrderEntity;
import com.example.shop.order.domain.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findAllByOrder(OrderEntity order);
}
