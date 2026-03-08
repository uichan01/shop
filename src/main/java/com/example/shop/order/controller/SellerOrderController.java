package com.example.shop.order.controller;

import com.example.shop._common.response.ApiResponse;
import com.example.shop.order.dto.request.UpdateOrderStatusRequest;
import com.example.shop.order.service.OrderService;
import com.example.shop.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seller/order")
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderService orderService;

    //주문 아이템 상태변경(SELLER)
    @PatchMapping("/item/{orderItemId}/status")
    public ApiResponse<Void> updateOrderItemStatus(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                   @PathVariable("orderItemId") Long orderItemId,
                                                   @Valid @RequestBody UpdateOrderStatusRequest request) {
        orderService.updateOrderItemStatus(currentUser.getUsername(), orderItemId, request.getStatus());
        return ApiResponse.success();
    }
}
