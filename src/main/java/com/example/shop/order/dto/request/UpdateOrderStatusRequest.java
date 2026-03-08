package com.example.shop.order.dto.request;

import com.example.shop.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull
    private OrderStatus status;
}
