package com.example.shop.product.dto.request;

import com.example.shop.product.domain.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ProductUpdateRequest {
    private Long id;
    private String name;
    private Long categoryId;
    private int price;
    private int stock;
    private Status status;
}
