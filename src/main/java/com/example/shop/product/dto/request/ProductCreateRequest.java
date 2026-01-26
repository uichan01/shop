package com.example.shop.product.dto.request;

import com.example.shop.product.domain.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductCreateRequest {
    private String name;
    private Long categoryId;
    private int price;
    private int stock;
    private Status status;
}
