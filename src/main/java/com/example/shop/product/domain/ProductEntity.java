package com.example.shop.product.domain;

import com.example.shop.category.domain.CategoryEntity;
import com.example.shop.member.domain.MemberEntity;
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
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private MemberEntity seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column(length =100, nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Column(length = 500)
    private String description;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public ProductEntity(MemberEntity seller, CategoryEntity category, String name, int price, int stock, String description, Status status) {
        this.seller = seller;
        this.category = category;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.status = status;
    }

    public void update(String name, Integer price, Integer stock, Status status, CategoryEntity category, String description) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.category = category;
        this.description = description;
    }

    public void delete() {
        this.status = Status.DELETED;
    }
}
