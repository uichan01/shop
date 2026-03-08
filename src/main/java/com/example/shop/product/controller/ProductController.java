package com.example.shop.product.controller;

import com.example.shop._common.response.ApiResponse;
import com.example.shop.product.dto.request.SearchOptionRequest;
import com.example.shop.product.dto.response.ProductDetailResponse;
import com.example.shop.product.dto.response.ProductListResponse;
import com.example.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    //단건 상세조회
    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getProduct(@PathVariable("productId") Long productId) {
        ProductDetailResponse product = productService.getProduct(productId);
        return ApiResponse.success(product);
    }

    //목록조회
    @GetMapping
    public ApiResponse<List<ProductListResponse>> getProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @ModelAttribute SearchOptionRequest searchOption) {
        return ApiResponse.success(
                productService.getProducts(searchOption, page, size)
        );
    }

    //인기상품 검색
}
