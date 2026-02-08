package com.example.shop.cart.repository;

import com.example.shop.cart.domain.CartEntity;
import com.example.shop.member.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByMember(MemberEntity member);
}
