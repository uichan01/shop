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
    private String categoryId;
    private String SellerId;

    private Integer page = 0;
    private Integer size = 20;
}
