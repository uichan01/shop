package com.example.shop.product.dto.request;

import com.example.shop.product.domain.ProductStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateRequest {
    private String name;
    private Long categoryId;
    private int price;
    private int stock;
    private ProductStatus productStatus;
    private String description;
}
