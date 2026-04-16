package com.example.shop.order.service;

import com.example.shop.cart.dto.request.AddCartItemRequest;
import com.example.shop.cart.repository.CartItemRepository;
import com.example.shop.cart.repository.CartRepository;
import com.example.shop.cart.service.CartService;
import com.example.shop.category.domain.CategoryEntity;
import com.example.shop.category.repository.CategoryRepository;
import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.domain.Role;
import com.example.shop.member.repository.MemberRepository;
import com.example.shop.order.repository.OrderItemRepository;
import com.example.shop.order.repository.OrderRepository;
import com.example.shop.product.domain.ProductEntity;
import com.example.shop.product.domain.ProductStatus;
import com.example.shop.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired private MemberRepository memberRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("동시 주문 - 100개의 스레드가 각각 3번씩 주문 시 최종 재고 0 검증")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentOrderStockDecrease() throws InterruptedException {
        // given: 재고 300개 상품 생성, 각 스레드에 독립적으로 할당된 100명의 회원 생성
        int threadCount = 100;
        int ordersPerThread = 3;
        int initialStock = threadCount * ordersPerThread; // 300

        MemberEntity seller = memberRepository.save(MemberEntity.builder()
                .name("동시성테스트셀러").email("concurrent_seller@test.com")
                .password(passwordEncoder.encode("password@")).role(Role.ROLE_SELLER).build());
        CategoryEntity category = categoryRepository.save(new CategoryEntity("동시성테스트카테고리"));
        ProductEntity product = productRepository.save(ProductEntity.builder()
                .seller(seller).category(category).name("동시성테스트상품")
                .price(1000).stock(initialStock).status(ProductStatus.SELLING).build());
        Long productId = product.getId();

        List<MemberEntity> members = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            members.add(memberRepository.save(MemberEntity.builder()
                    .name("동시성테스트유저" + i)
                    .email("concurrent_member" + i + "@test.com")
                    .password(passwordEncoder.encode("password@"))
                    .role(Role.ROLE_USER).build()));
        }

        // when: 100개 스레드 동시 시작, 각 스레드는 addToCart → createOrder 를 3번 반복
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);  // 전 스레드 동시 출발용
        CountDownLatch doneLatch = new CountDownLatch(threadCount); // 전 스레드 완료 대기용
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String email = members.get(i).getEmail();
            executor.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 동시에 시작하도록 대기
                    for (int j = 0; j < ordersPerThread; j++) {
                        // 주문 전 장바구니에 상품 1개 담기 (이전 주문에서 cart item이 삭제됐으므로 매번 추가)
                        cartService.addProductToMyCart(email, new AddCartItemRequest(productId, 1));
                        // 주문 생성: decreaseStock(WHERE stock >= quantity) 조건부 UPDATE 실행
                        orderService.createOrder(email, "서울시 강남구 테헤란로 123");
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 전 스레드 동시 출발
        boolean completed = doneLatch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        // then: 타임아웃 없이 전 스레드 완료, 예외 없음, 최종 재고 정확히 0
        assertThat(completed).as("60초 내 모든 스레드가 완료되어야 합니다").isTrue();
        assertThat(errorCount.get()).as("재고 부족 등 예외가 발생하지 않아야 합니다").isEqualTo(0);

        ProductEntity finalProduct = productRepository.findById(productId).orElseThrow();
        assertThat(finalProduct.getStock()).as("300번 주문 후 최종 재고는 0이어야 합니다").isEqualTo(0);

        // cleanup: FK 제약 순서대로 삭제 (order_items → orders → cart_items → carts → members → product → seller → category)
        members.forEach(member -> {
            orderRepository.findAllByMember(member).forEach(order ->
                    orderItemRepository.deleteAll(orderItemRepository.findAllByOrder(order)));
            orderRepository.deleteAll(orderRepository.findAllByMember(member));
            cartRepository.findByMember(member).ifPresent(cart -> {
                cartItemRepository.deleteAll(cartItemRepository.findAllByCart(cart));
                cartRepository.delete(cart);
            });
            memberRepository.delete(member);
        });
        productRepository.deleteById(productId);
        memberRepository.delete(seller);
        categoryRepository.delete(category);
    }
}
