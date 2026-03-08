package com.example.shop.order.controller;

import com.example.shop._common.response.ApiResponse;
import com.example.shop.order.dto.request.CreateOrderRequest;
import com.example.shop.order.dto.response.OrderResultResponse;
import com.example.shop.order.service.OrderService;
import com.example.shop.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    //주문 생성
    @PostMapping
    public ApiResponse<OrderResultResponse> createOrder(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                        @Valid @RequestBody CreateOrderRequest request) {
        OrderResultResponse response = orderService.createOrder(currentUser.getUsername(), request.getAddress());
        return ApiResponse.success(response);
    }

    //주문 단건조회
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResultResponse> getOrder(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                     @PathVariable("orderId") Long orderId) {
        OrderResultResponse response = orderService.getOrder(currentUser.getUsername(), orderId);
        return ApiResponse.success(response);
    }

    //본인 주문 목록조회
    @GetMapping
    public ApiResponse<List<OrderResultResponse>> getMyOrders(@AuthenticationPrincipal CustomUserDetails currentUser) {
        List<OrderResultResponse> response = orderService.getMyOrders(currentUser.getUsername());
        return ApiResponse.success(response);
    }

    //주문 아이템 취소
    @DeleteMapping("/{orderId}/item/{orderItemId}")
    public ApiResponse<Void> cancelOrderItem(@AuthenticationPrincipal CustomUserDetails currentUser,
                                             @PathVariable("orderId") Long orderId,
                                             @PathVariable("orderItemId") Long orderItemId) {
        orderService.cancelOrderItem(currentUser.getUsername(), orderId, orderItemId);
        return ApiResponse.success();
    }
}
