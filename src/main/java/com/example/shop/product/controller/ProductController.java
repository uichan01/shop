package com.example.shop.product.controller;

import com.example.shop._common.response.ApiResponse;
import com.example.shop.product.dto.request.ProductCreateRequest;
import com.example.shop.product.dto.request.ProductUpdateRequest;
import com.example.shop.product.dto.request.SearchOptionRequest;
import com.example.shop.product.dto.response.ProductDetailResponse;
import com.example.shop.product.dto.response.ProductListResponse;
import com.example.shop.product.service.ProductService;
import com.example.shop.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    //상품등록(SELLER)
    @PostMapping
    public ApiResponse<Long> registerProduct(@AuthenticationPrincipal CustomUserDetails currentUser,
                                             @RequestPart("product") ProductCreateRequest request,
                                             @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long productId = productService.registerProduct(request, currentUser.getUsername(), images);
        return ApiResponse.success(productId);
    }

    //상품수정(SELLER)
    @PutMapping
    public ApiResponse<Long> updateProduct(@AuthenticationPrincipal CustomUserDetails currentUser,
                                           @RequestPart("product") ProductUpdateRequest request,
                                           @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long productId = productService.updateProduct(request, currentUser.getUsername(), images);
        return ApiResponse.success(productId);
    }

    //상품삭제(SELLER)
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(@AuthenticationPrincipal CustomUserDetails currentUser,
                                           @PathVariable("productId") Long productId) {
        productService.deleteProduct(productId, currentUser.getUsername());
        return ApiResponse.success();
    }

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
