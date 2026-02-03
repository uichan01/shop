package com.example.shop.product.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductListResponse {

    private Long productId;

    private String name;
    private int price;

    private String thumbnailPath;
}
