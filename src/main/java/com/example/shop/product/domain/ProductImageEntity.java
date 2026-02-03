package com.example.shop.product.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product_images")
@EntityListeners(AuditingEntityListener.class)
public class ProductImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "photo_path", nullable = false)
    private String photoPath;

    // 여러 이미지일 때 이미지 순서
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public ProductImageEntity(ProductEntity product, String photoUrl, int sortOrder) {
        this.product = product;
        this.photoPath = photoUrl;
        this.sortOrder = sortOrder;
    }
}
