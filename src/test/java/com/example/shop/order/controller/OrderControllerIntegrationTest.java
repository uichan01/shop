package com.example.shop.order.controller;

import com.example.shop.cart.domain.CartEntity;
import com.example.shop.cart.domain.CartItemEntity;
import com.example.shop.cart.repository.CartItemRepository;
import com.example.shop.cart.repository.CartRepository;
import com.example.shop.category.domain.CategoryEntity;
import com.example.shop.category.repository.CategoryRepository;
import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.domain.Role;
import com.example.shop.member.repository.MemberRepository;
import com.example.shop.order.domain.OrderStatus;
import com.example.shop.order.repository.OrderRepository;
import com.example.shop.product.domain.ProductEntity;
import com.example.shop.product.domain.ProductStatus;
import com.example.shop.product.repository.ProductRepository;
import com.example.shop.security.dto.CustomUserDetails;
import com.example.shop.security.dto.MemberDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIntegrationTest {

    @Getter
    @Setter
    @Builder
    static class TestData {
        private MemberEntity member;
        private MemberEntity seller;
        private ProductEntity product;
        private ProductEntity product2;
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private MemberEntity saveTestMember() {
        return memberRepository.save(
                MemberEntity.builder()
                        .name("테스트유저")
                        .email("member@test.com")
                        .password(passwordEncoder.encode("password@"))
                        .role(Role.ROLE_USER)
                        .build()
        );
    }

    private MemberEntity saveSeller() {
        return memberRepository.save(
                MemberEntity.builder()
                        .name("테스트셀러")
                        .email("seller@test.com")
                        .password(passwordEncoder.encode("password@"))
                        .role(Role.ROLE_SELLER)
                        .build()
        );
    }

    private CategoryEntity saveCategory(String name) {
        return categoryRepository.save(new CategoryEntity(name));
    }

    private ProductEntity saveProduct(MemberEntity seller, CategoryEntity category, String name, int price, int stock) {
        return productRepository.save(
                ProductEntity.builder()
                        .seller(seller)
                        .category(category)
                        .name(name)
                        .price(price)
                        .stock(stock)
                        .description("설명")
                        .status(ProductStatus.SELLING)
                        .build()
        );
    }

    private Authentication buildAuthentication(MemberEntity member) {
        CustomUserDetails userDetails = new CustomUserDetails(
                new MemberDto(
                        member.getEmail(),
                        member.getPassword(),
                        member.getRole().name()
                )
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private void setSecurityContext(MemberEntity member) {
        SecurityContextHolder.getContext().setAuthentication(buildAuthentication(member));
    }

    private void addToCart(MemberEntity member, ProductEntity product, int quantity) {
        CartEntity cart = cartRepository.findByMember(member)
                .orElseGet(() -> cartRepository.save(new CartEntity(member)));
        cartItemRepository.save(
                CartItemEntity.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(quantity)
                        .build()
        );
    }

    private TestData getTestData() {
        MemberEntity member = saveTestMember();
        MemberEntity seller = saveSeller();
        CategoryEntity category = saveCategory("카테고리1");
        ProductEntity product = saveProduct(seller, category, "테스트상품1", 10000, 100);
        ProductEntity product2 = saveProduct(seller, category, "테스트상품2", 20000, 50);

        setSecurityContext(member);

        return TestData.builder()
                .member(member)
                .seller(seller)
                .product(product)
                .product2(product2)
                .build();
    }

    @Test
    @DisplayName("주문 생성 - 장바구니 상품으로 주문")
    void createOrder() throws Exception {
        // given: member 로그인 상태, 장바구니에 테스트상품1 2개 담음
        TestData testData = getTestData();
        addToCart(testData.getMember(), testData.getProduct(), 2);
        Map<String, String> request = Map.of("address", "서울시 강남구 테헤란로 123");

        // when: POST /order 주문 생성 요청
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then: 200 반환, 주문 정보(orderNumber, address, totalPrice, orderItems) 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.address").value("서울시 강남구 테헤란로 123"))
                .andExpect(jsonPath("$.data.totalPrice").value(20000))              // 10,000 x 2
                .andExpect(jsonPath("$.data.orderItems", hasSize(1)))
                .andExpect(jsonPath("$.data.orderItems[0].productName").value("테스트상품1"))
                .andExpect(jsonPath("$.data.orderItems[0].unitPrice").value(10000)) // 주문 당시 단가 고정
                .andExpect(jsonPath("$.data.orderItems[0].quantity").value(2));
    }

    @Test
    @DisplayName("주문 생성 - 장바구니 상품 여러 개로 주문")
    void createOrderWithMultipleItems() throws Exception {
        // given: member 로그인 상태, 장바구니에 상품 2종 담음 (10,000원 x1, 20,000원 x2)
        TestData testData = getTestData();
        addToCart(testData.getMember(), testData.getProduct(), 1);
        addToCart(testData.getMember(), testData.getProduct2(), 2);
        Map<String, String> request = Map.of("address", "서울시 강남구 테헤란로 123");

        // when: POST /order 주문 생성 요청
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then: 200 반환, orderItems 2건, totalPrice = 10000*1 + 20000*2 = 50,000원
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderItems", hasSize(2)))
                .andExpect(jsonPath("$.data.totalPrice").value(50000));
    }

    @Test
    @DisplayName("주문 생성 - 빈 장바구니로 주문 시 실패")
    void createOrderWithEmptyCart() throws Exception {
        // given: member 로그인 상태, 장바구니에 아무것도 담지 않음
        getTestData();
        Map<String, String> request = Map.of("address", "서울시 강남구 테헤란로 123");

        // when: POST /order 주문 생성 요청
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then: IllegalStateException → GlobalExceptionHandler → 400 반환
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("상품 주문 실패 - 재고수량 0 이하")
    void createOrderWithNoStock() throws Exception {
        // given: 재고 5개인 상품 생성, member가 장바구니에 4개 담고 첫 번째 주문 완료 (재고: 5 → 1)
        MemberEntity member = saveTestMember();
        MemberEntity seller = saveSeller();
        CategoryEntity category = saveCategory("카테고리1");
        ProductEntity product = saveProduct(seller, category, "재고부족상품", 10000, 5);
        addToCart(member, product, 4);
        Map<String, String> request = Map.of("address", "서울시 강남구 테헤란로 123");
        mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // when: 남은 재고(1개)보다 많은 수량(3개)을 장바구니에 담고 주문 시도
        cartItemRepository.flush();
        addToCart(member, product, 3);

        // then: decreaseStock WHERE stock >= quantity 불충족 → IllegalStateException("재고가 부족합니다.") → 400 반환
        mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(member)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 단건 조회")
    void getOrder() throws Exception {
        // given: member 로그인 상태, 장바구니에 상품 담고 주문 생성 완료
        TestData testData = getTestData();
        addToCart(testData.getMember(), testData.getProduct(), 1);
        Map<String, String> createRequest = Map.of("address", "서울시 강남구 테헤란로 123");
        String createResponse = mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(testData.getMember())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        // when: GET /order/{orderId} 단건 조회 요청
        mockMvc.perform(get("/order/{orderId}", orderId)
                        .with(authentication(buildAuthentication(testData.getMember()))))
                // then: 200 반환, 주문번호/주소/금액 일치 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.address").value("서울시 강남구 테헤란로 123"))
                .andExpect(jsonPath("$.data.totalPrice").value(10000));
    }

    @Test
    @DisplayName("주문 단건 조회 - 타인의 주문 조회 시 실패")
    void getOtherMemberOrder() throws Exception {
        // given: member가 주문 생성, otherMember는 해당 주문과 무관한 다른 유저
        TestData testData = getTestData();
        addToCart(testData.getMember(), testData.getProduct(), 1);
        Map<String, String> createRequest = Map.of("address", "서울시 강남구 테헤란로 123");
        String createResponse = mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(testData.getMember())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).path("data").path("id").asLong();
        MemberEntity otherMember = memberRepository.save(
                MemberEntity.builder()
                        .name("다른유저")
                        .email("other@test.com")
                        .password(passwordEncoder.encode("password@"))
                        .role(Role.ROLE_USER)
                        .build()
        );

        // when: otherMember 컨텍스트로 GET /order/{orderId} 요청
        mockMvc.perform(get("/order/{orderId}", orderId)
                        .with(authentication(buildAuthentication(otherMember))))
                // then: findByIdAndMember(orderId, otherMember) → empty → NoSuchElementException → 400
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("본인 주문 목록 조회")
    void getMyOrders() throws Exception {
        // given: member가 주문 2회 생성 (주문 시 장바구니가 비워지므로 각각 담고 주문)
        TestData testData = getTestData();
        addToCart(testData.getMember(), testData.getProduct(), 1);
        Map<String, String> createRequest = Map.of("address", "서울시 강남구 테헤란로 123");
        mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(testData.getMember())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());
        addToCart(testData.getMember(), testData.getProduct2(), 1);
        mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(testData.getMember())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        // when: GET /order 본인 주문 목록 조회 요청
        mockMvc.perform(get("/order")
                        .with(authentication(buildAuthentication(testData.getMember()))))
                // then: 200 반환, member 본인 주문 2건만 조회됨
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("주문 아이템 취소 - 단건 아이템 취소 성공")
    void cancelOrderItem() throws Exception {
        // given: member가 상품 2종을 장바구니에 담고 주문 생성 완료
        TestData testData = getTestData();
        addToCart(testData.getMember(), testData.getProduct(), 1);
        addToCart(testData.getMember(), testData.getProduct2(), 2);
        Map<String, String> createRequest = Map.of("address", "서울시 강남구 테헤란로 123");
        String createResponse = mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(testData.getMember())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).path("data").path("id").asLong();
        Long orderItemId = objectMapper.readTree(createResponse).path("data").path("orderItems").get(0).path("id").asLong();

        // when: DELETE /order/{orderId}/item/{orderItemId} 첫 번째 아이템만 취소 요청
        mockMvc.perform(delete("/order/{orderId}/item/{orderItemId}", orderId, orderItemId)
                        .with(authentication(buildAuthentication(testData.getMember()))))
                // then: 200 반환
                .andExpect(status().isOk());

        // then: 단건 조회 시 첫 번째 아이템만 CANCELED, 두 번째 아이템은 취소 안 됨
        mockMvc.perform(get("/order/{orderId}", orderId)
                        .with(authentication(buildAuthentication(testData.getMember()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderItems[0].status").value(OrderStatus.CANCELED.name()))
                .andExpect(jsonPath("$.data.orderItems[1].status").value(OrderStatus.PAYMENT_PENDING.name()));
    }

    @Test
    @DisplayName("주문 아이템 취소 - 이미 취소된 아이템 재취소 시 실패")
    void cancelOrderItemAlreadyCanceled() throws Exception {
        // given: member가 주문 생성 후 첫 번째 아이템을 이미 취소한 상태
        TestData testData = getTestData();
        addToCart(testData.getMember(), testData.getProduct(), 1);
        Map<String, String> createRequest = Map.of("address", "서울시 강남구 테헤란로 123");
        String createResponse = mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(testData.getMember())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).path("data").path("id").asLong();
        Long orderItemId = objectMapper.readTree(createResponse).path("data").path("orderItems").get(0).path("id").asLong();
        mockMvc.perform(delete("/order/{orderId}/item/{orderItemId}", orderId, orderItemId)
                        .with(authentication(buildAuthentication(testData.getMember()))))
                .andExpect(status().isOk());

        // when: 같은 아이템을 한 번 더 취소 요청
        mockMvc.perform(delete("/order/{orderId}/item/{orderItemId}", orderId, orderItemId)
                        .with(authentication(buildAuthentication(testData.getMember()))))
                // then: IllegalStateException → GlobalExceptionHandler → 400
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 아이템 취소 - 타인의 주문 아이템 취소 시 실패")
    void cancelOrderItemByOtherMember() throws Exception {
        // given: member가 주문 생성, otherMember는 해당 주문과 무관
        TestData testData = getTestData();
        addToCart(testData.getMember(), testData.getProduct(), 1);
        Map<String, String> createRequest = Map.of("address", "서울시 강남구 테헤란로 123");
        String createResponse = mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(testData.getMember())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).path("data").path("id").asLong();
        Long orderItemId = objectMapper.readTree(createResponse).path("data").path("orderItems").get(0).path("id").asLong();
        MemberEntity otherMember = memberRepository.save(
                MemberEntity.builder()
                        .name("다른유저")
                        .email("other@test.com")
                        .password(passwordEncoder.encode("password@"))
                        .role(Role.ROLE_USER)
                        .build()
        );

        // when: otherMember 컨텍스트로 DELETE /order/{orderId}/item/{orderItemId} 요청
        mockMvc.perform(delete("/order/{orderId}/item/{orderItemId}", orderId, orderItemId)
                        .with(authentication(buildAuthentication(otherMember))))
                // then: findByIdAndMember(orderId, otherMember) → empty → NoSuchElementException → 400
                .andExpect(status().isBadRequest());
    }

    

    @Test
    @DisplayName("주문 아이템 상태 변경 - 셀러 권한으로 SHIPPED 변경 성공")
    void updateOrderItemStatus() throws Exception {
        // given: member가 주문 생성 완료, orderItemId 파싱
        TestData testData = getTestData();
        addToCart(testData.getMember(), testData.getProduct(), 1);
        Map<String, String> createRequest = Map.of("address", "서울시 강남구 테헤란로 123");
        String createResponse = mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(testData.getMember())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).path("data").path("id").asLong();
        Long orderItemId = objectMapper.readTree(createResponse).path("data").path("orderItems").get(0).path("id").asLong();
        Map<String, String> statusRequest = Map.of("status", OrderStatus.SHIPPED.name());

        // when: seller 컨텍스트로 PATCH /seller/order/item/{orderItemId}/status 요청
        mockMvc.perform(patch("/seller/order/item/{orderItemId}/status", orderItemId)
                        .with(authentication(buildAuthentication(testData.getSeller())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                // then: 200 반환
                .andExpect(status().isOk());

        // then: 단건 조회 시 해당 아이템 status = SHIPPED 확인
        mockMvc.perform(get("/order/{orderId}", orderId)
                        .with(authentication(buildAuthentication(testData.getMember()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderItems[0].status").value(OrderStatus.SHIPPED.name()));
    }

    @Test
    @DisplayName("주문 아이템 상태 변경 - 해당 상품의 판매자가 아닌 셀러로 시도 시 실패")
    void updateOrderItemStatusByOtherSeller() throws Exception {
        // given: member가 주문 생성, otherSeller는 해당 상품의 판매자가 아님
        TestData testData = getTestData();
        addToCart(testData.getMember(), testData.getProduct(), 1);
        Map<String, String> createRequest = Map.of("address", "서울시 강남구 테헤란로 123");
        String createResponse = mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(testData.getMember())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long orderItemId = objectMapper.readTree(createResponse).path("data").path("orderItems").get(0).path("id").asLong();
        MemberEntity otherSeller = memberRepository.save(
                MemberEntity.builder()
                        .name("다른셀러")
                        .email("otherseller@test.com")
                        .password(passwordEncoder.encode("password@"))
                        .role(Role.ROLE_SELLER)
                        .build()
        );
        Map<String, String> statusRequest = Map.of("status", OrderStatus.SHIPPED.name());

        // when: otherSeller 컨텍스트로 PATCH /seller/order/item/{orderItemId}/status 요청
        mockMvc.perform(patch("/seller/order/item/{orderItemId}/status", orderItemId)
                        .with(authentication(buildAuthentication(otherSeller)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                // then: IllegalArgumentException → GlobalExceptionHandler → 400
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주문 상태 변경 - 일반 유저 권한으로 시도 시 실패")
    void updateOrderStatusByUser() throws Exception {
        // given: member(ROLE_USER)가 주문 생성
        TestData testData = getTestData();
        addToCart(testData.getMember(), testData.getProduct(), 1);
        Map<String, String> createRequest = Map.of("address", "서울시 강남구 테헤란로 123");
        String createResponse = mockMvc.perform(post("/order")
                        .with(authentication(buildAuthentication(testData.getMember())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(createResponse).path("data").path("id").asLong();
        Map<String, String> statusRequest = Map.of("status", OrderStatus.SHIPPED.name());

        // when: member(ROLE_USER) 컨텍스트로 PATCH /seller/order/item/{orderId}/status 요청
        mockMvc.perform(patch("/seller/order/item/{orderItemId}/status", orderId)
                        .with(authentication(buildAuthentication(testData.getMember())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                // then: SecurityConfig /seller/** → hasRole("SELLER") → ROLE_USER 차단 → 403
                .andExpect(status().isForbidden());
    }
}
