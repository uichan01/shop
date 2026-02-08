package com.example.shop.cart.controller;

import com.example.shop.cart.dto.request.AddCartItemRequest;
import com.example.shop.cart.repository.CartItemRepository;
import com.example.shop.cart.repository.CartRepository;
import com.example.shop.category.domain.CategoryEntity;
import com.example.shop.category.repository.CategoryRepository;
import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.domain.Role;
import com.example.shop.member.repository.MemberRepository;
import com.example.shop.product.domain.ProductEntity;
import com.example.shop.product.domain.Status;
import com.example.shop.product.repository.ProductRepository;
import com.example.shop.security.dto.CustomUserDetails;
import com.example.shop.security.dto.MemberDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CartControllerIntegrationTest {

    @Getter
    @Setter
    @Builder
    static class TestData {
        private MemberEntity member;
        private ProductEntity product;
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
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
//        productRepository.deleteAll();
//        memberRepository.deleteAll();
//        categoryRepository.deleteAll();
//        cartItemRepository.deleteAll();
//        cartRepository.deleteAll();

        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();

        SecurityContextHolder.clearContext();
    }

    private MemberEntity saveTestMember() {
        return memberRepository.save(
                MemberEntity.builder()
                        .name("테스트유저")
                        .email("membber@test.com")
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

    private CategoryEntity saveCategory(String categoryName) {
        CategoryEntity category = new CategoryEntity(categoryName);
        return categoryRepository.save(category);
    }

    private void setSecurityContext(MemberEntity member) {
        CustomUserDetails userDetails = new CustomUserDetails(
                new MemberDto(
                        member.getEmail(),
                        member.getPassword(),
                        member.getRole().name()
                )
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public TestData getTestData() {
        MemberEntity member = saveTestMember();
        MemberEntity seller = saveSeller();
        setSecurityContext(member);

        CategoryEntity category1 = saveCategory("카테고리1");
        ProductEntity product = ProductEntity.builder()
                .seller(seller)
                .category(category1)
                .name("테스트상품1")
                .price(10000)
                .stock(100)
                .description("설명")
                .status(Status.SELLING)
                .build();

        productRepository.save(product);

        TestData testData = TestData.builder()
                .member(member)
                .product(product)
                .build();

        return testData;
    }

    @Test
    @DisplayName("빈 카트 조회")
    void getEmptyCart() throws Exception {
        TestData testData = getTestData();

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(0)))
                .andExpect(jsonPath("$.data.totalQuantity").value(0))
                .andExpect(jsonPath("$.data.totalPrice").value(0));
    }

    @Test
    @DisplayName("카트 상품추가")
    void getNotEmptyCart() throws Exception {
        TestData testData = getTestData();
        AddCartItemRequest request = AddCartItemRequest.builder()
                .productId(testData.product.getId())
                .quantity(1)
                .build();

        mockMvc.perform(post("/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
