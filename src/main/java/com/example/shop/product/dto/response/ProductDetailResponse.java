package com.example.shop.product.dto.response;

import com.example.shop.product.domain.ProductStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDetailResponse {

    private Long productId;

    private String name;
    private int price;
    private String description;

    private ProductStatus productStatus;

    private List<ProductImageResponse> images;
}
