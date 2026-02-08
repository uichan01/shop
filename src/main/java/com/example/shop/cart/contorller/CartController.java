package com.example.shop.cart.contorller;

import com.example.shop._common.response.ApiResponse;
import com.example.shop.cart.dto.request.AddCartItemRequest;
import com.example.shop.cart.dto.request.UpdateCartItemRequest;
import com.example.shop.cart.dto.response.CartInfoResponse;
import com.example.shop.cart.service.CartService;
import com.example.shop.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    //본인 카트조회
    private final CartService cartService;
    @GetMapping
    public ApiResponse<CartInfoResponse> getCartInfo(@AuthenticationPrincipal CustomUserDetails currentUser) {
        CartInfoResponse cartInfo = cartService.getMyCart(currentUser.getUsername());
        return ApiResponse.success(cartInfo);
    }
    //카트 상품추가
    @PostMapping("/items")
    public ApiResponse<Void> addCartItem(@AuthenticationPrincipal CustomUserDetails currentUser,
                                         @RequestBody AddCartItemRequest request) {
        cartService.addProductToMyCart(currentUser.getUsername(), request);
        return ApiResponse.success();
    }
    //카트 상품삭제
    @DeleteMapping("/items/{productId}")
    public ApiResponse<Void> deleteCartItem(@AuthenticationPrincipal CustomUserDetails currentUser,
                                            @PathVariable("productId") Long productId) {
        cartService.removeProductFromMyCart(currentUser.getUsername(), productId);
        return ApiResponse.success();
    }
    //카트 수량변경
    @PutMapping("/items")
    public ApiResponse<Void> updateCartItem(@AuthenticationPrincipal CustomUserDetails currentUser,
                                            @RequestBody UpdateCartItemRequest request) {
        cartService.updateMyCartProductQuantity(currentUser.getUsername(), request);
        return ApiResponse.success();
    }
}
