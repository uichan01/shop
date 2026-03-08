package com.example.shop.order.domain;

import com.example.shop.member.domain.MemberEntity;
import com.example.shop.product.domain.ProductEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@Table(name = "order_items")
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private MemberEntity seller;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Builder
    public OrderItemEntity(MemberEntity seller, OrderStatus status, OrderEntity order, ProductEntity product, int unitPrice, int quantity) {
        this.seller = seller;
        this.status = status;
        this.order = order;
        this.product = product;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }
}
