package com.example.shop.product.controller;

import com.example.shop._common.response.ApiResponse;
import com.example.shop.product.dto.request.ProductCreateRequest;
import com.example.shop.product.dto.request.SearchOptionRequest;
import com.example.shop.product.dto.response.ProductDetailResponse;
import com.example.shop.product.dto.response.ProductListResponse;
import com.example.shop.product.service.ProductService;
import com.example.shop.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    //상품등록(SELLER)
    @PostMapping
    public ApiResponse<Long> registerProduct(@AuthenticationPrincipal CustomUserDetails currentUser, ProductCreateRequest request) {

    }
    //상품수정(SELLER)
    @PutMapping("/{productId")
    public ApiResponse<Long> updateProduct() {

    }
    //상품삭제(SELLER)
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(
            @PathVariable Long productId
    ) { }

    //단건 상세조회
    @GetMapping("/{productId}")
    public ApiResponse<ProductDetailResponse> getProduct(
            @PathVariable Long productId
    ) { }

    //목록조회
    @GetMapping
    public ApiResponse<List<ProductListResponse>> getProducts(SearchOptionRequest searchOption) {

    }



    //인기상품 검색
}
