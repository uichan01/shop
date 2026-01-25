package com.example.shop.product.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchOptionRequest {
    private String keyword;
    private String categoryId;
    private String SellerId;

    private Integer page = 0;
    private Integer size = 20;
}
