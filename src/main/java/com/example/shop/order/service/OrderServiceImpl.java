package com.example.shop.order.service;

import com.example.shop.cart.domain.CartEntity;
import com.example.shop.cart.domain.CartItemEntity;
import com.example.shop.cart.repository.CartItemRepository;
import com.example.shop.cart.repository.CartRepository;
import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.repository.MemberRepository;
import com.example.shop.order.domain.OrderEntity;
import com.example.shop.order.domain.OrderItemEntity;
import com.example.shop.order.domain.OrderStatus;
import com.example.shop.order.dto.response.OrderItemResponse;
import com.example.shop.order.dto.response.OrderResultResponse;
import com.example.shop.order.repository.OrderItemRepository;
import com.example.shop.order.repository.OrderRepository;
import com.example.shop.product.domain.ProductEntity;
import com.example.shop.product.mapper.ProductMapper;
import com.example.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public OrderResultResponse createOrder(String email, String address) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        CartEntity cart = cartRepository.findByMember(member)
                .orElseThrow(() -> new NoSuchElementException("장바구니가 존재하지 않습니다."));

        List<CartItemEntity> cartItems = cartItemRepository.findAllByCart(cart);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("장바구니가 비어있습니다.");
        }

        int totalPrice = 0;
        for (CartItemEntity cartItem : cartItems) {
            ProductEntity product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

            int updatedRow = productMapper.decreaseStock(product.getId(), cartItem.getQuantity());

            if (updatedRow == 0) {
                throw new IllegalStateException("재고가 부족합니다.");
            }

            totalPrice += product.getPrice() * cartItem.getQuantity();
        }

        OrderEntity order = OrderEntity.builder()
                .member(member)
                .orderNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 20))
                .address(address)
                .totalPrice(totalPrice)
                .build();
        orderRepository.save(order);

        List<OrderItemEntity> orderItems = cartItems.stream()
                .map(cartItem -> OrderItemEntity.builder()
                        .order(order)
                        .seller(cartItem.getProduct().getSeller())
                        .product(cartItem.getProduct())
                        .unitPrice(cartItem.getProduct().getPrice())
                        .quantity(cartItem.getQuantity())
                        .status(OrderStatus.PAYMENT_PENDING)
                        .build())
                .toList();
        orderItemRepository.saveAll(orderItems);

        cartItemRepository.deleteAll(cartItems);

        return toResponse(order, orderItems);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResultResponse getOrder(String email, Long orderId) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        OrderEntity order = orderRepository.findByIdAndMember(orderId, member)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));

        List<OrderItemEntity> orderItems = orderItemRepository.findAllByOrder(order);
        return toResponse(order, orderItems);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResultResponse> getMyOrders(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        return orderRepository.findAllByMember(member).stream()
                .map(order -> toResponse(order, orderItemRepository.findAllByOrder(order)))
                .toList();
    }

    @Override
    @Transactional
    public void cancelOrderItem(String email, Long orderId, Long orderItemId) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        orderRepository.findByIdAndMember(orderId, member)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다."));

        OrderItemEntity item = orderItemRepository.findById(orderItemId)
                .filter(i -> i.getOrder().getId().equals(orderId))
                .orElseThrow(() -> new NoSuchElementException("주문 아이템을 찾을 수 없습니다."));

        if (item.getStatus() == OrderStatus.SHIPPED || item.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("이미 배송 중이거나 완료된 아이템은 취소할 수 없습니다.");
        }
        if (item.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 아이템입니다.");
        }

        ProductEntity product = productRepository.findById(item.getProduct().getId())
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));
        productMapper.increaseStock(item.getProduct().getId(), item.getQuantity());
        item.cancel();
    }

    @Override
    @Transactional
    public void updateOrderItemStatus(String email, Long orderItemId, OrderStatus status) {
        MemberEntity seller = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        OrderItemEntity orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NoSuchElementException("주문 아이템을 찾을 수 없습니다."));

        if (!orderItem.getSeller().getId().equals(seller.getId())) {
            throw new IllegalArgumentException("해당 주문 아이템의 판매자가 아닙니다.");
        }

        orderItem.updateStatus(status);
    }

    private OrderResultResponse toResponse(OrderEntity order, List<OrderItemEntity> orderItems) {
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productName(item.getProduct().getName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .status(item.getStatus())
                        .build())
                .toList();

        return OrderResultResponse.builder()
                .id(order.getId())
                .orderItems(itemResponses)
                .orderNumber(order.getOrderNumber())
                .address(order.getAddress())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
