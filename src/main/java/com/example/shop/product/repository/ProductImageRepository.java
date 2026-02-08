package com.example.shop.product.repository;

import com.example.shop.product.domain.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {
    void deleteByProductId(Long productId);

    List<ProductImageEntity> findByProductIdOrderBySortOrderAsc(Long productId);

    Optional<ProductImageEntity> findFirstByProductIdOrderBySortOrderAsc(Long productId);
}
