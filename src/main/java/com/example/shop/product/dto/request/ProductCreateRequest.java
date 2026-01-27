package com.example.shop.product.dto.request;

import com.example.shop.product.domain.Status;
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
    private Status status;
}
