package com.example.shop.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchOptionRequest {
    private String keyword;
    private Long categoryId;
    private Long SellerId;
}
