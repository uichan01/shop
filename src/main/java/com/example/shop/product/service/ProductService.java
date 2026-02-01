package com.example.shop.product.service;

import com.example.shop.product.dto.request.ProductCreateRequest;
import com.example.shop.product.dto.request.ProductUpdateRequest;
import com.example.shop.product.dto.request.SearchOptionRequest;
import com.example.shop.product.dto.response.ProductDetailResponse;
import com.example.shop.product.dto.response.ProductListResponse;

import java.util.List;

public interface ProductService {

    Long registerProduct(ProductCreateRequest request, String sellerName);

    Long updateProduct(ProductUpdateRequest request, String sellerName);

    void deleteProduct(Long productId, String sellerName);

    ProductDetailResponse getProduct(Long productId);

    List<ProductListResponse> getProducts(SearchOptionRequest searchOption, int page, int size);
}
