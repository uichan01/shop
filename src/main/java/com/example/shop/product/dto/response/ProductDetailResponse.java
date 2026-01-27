package com.example.shop.product.dto.response;

import com.example.shop.product.domain.Status;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDetailResponse {
    private String name;
    private int price;
}
