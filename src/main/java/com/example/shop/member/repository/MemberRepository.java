package com.example.shop.member.repository;

import com.example.shop.member.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Boolean existsByEmail(String email);

    Optional<MemberEntity> findByEmail(String email);

    long deleteByEmail(String email);
}
