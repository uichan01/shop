package com.example.shop.order.repository;

import com.example.shop.member.domain.MemberEntity;
import com.example.shop.order.domain.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByIdAndMember(Long id, MemberEntity member);
    List<OrderEntity> findAllByMember(MemberEntity member);
}
