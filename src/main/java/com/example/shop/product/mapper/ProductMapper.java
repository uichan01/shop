package com.example.shop.product.mapper;

import com.example.shop.product.dto.request.SearchOptionRequest;
import com.example.shop.product.dto.response.ProductListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    List<ProductListResponse> findProductListWithOption(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("request") SearchOptionRequest request
    );
}