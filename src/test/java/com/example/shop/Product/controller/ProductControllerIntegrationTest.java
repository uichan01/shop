package com.example.shop.product.controller;

import com.example.shop.category.domain.CategoryEntity;
import com.example.shop.category.repository.CategoryRepository;
import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.domain.Role;
import com.example.shop.member.repository.MemberRepository;
import com.example.shop.product.domain.Status;
import com.example.shop.product.dto.request.ProductCreateRequest;
import com.example.shop.product.repository.ProductRepository;
import com.example.shop.security.dto.CustomUserDetails;
import com.example.shop.security.dto.MemberDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        memberRepository.deleteAll();
        SecurityContextHolder.clearContext();
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
    private CategoryEntity saveCategory() {
        CategoryEntity category = new CategoryEntity("테스트카테고리");
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


    @Test
    @DisplayName("상품 등록 테스트 (SELLER)")
    void registerProduct() throws Exception {
        MemberEntity seller = saveSeller();
        setSecurityContext(seller);

        CategoryEntity category = saveCategory();

        ProductCreateRequest req = new ProductCreateRequest(
                "테스트상품",
                category.getId(),
                10000,
                10,
                Status.SELLING
        );

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }



    @Test
    @DisplayName("상품 단건 조회 테스트")
    void getProductDetail() throws Exception {
        MemberEntity member = saveSeller();
        setSecurityContext(member);

        ProductCreateRequest req = new ProductCreateRequest(
                "테스트상품",
                null,
                10000,
                10,
                Status.SELLING
        );

        String response =
                mockMvc.perform(post("/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long productId = objectMapper
                .readTree(response)
                .get("data")
                .asLong();

        mockMvc.perform(get("/product/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("테스트상품"));
    }

    @Test
    @DisplayName("상품 목록 조회 테스트")
    void getProductList() throws Exception {
        MemberEntity member = saveTestMember();
        setSecurityContext(member);

        mockMvc.perform(get("/product")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("상품 삭제 테스트 (SELLER)")
    void deleteProduct() throws Exception {
        MemberEntity seller = saveSeller();
        setSecurityContext(seller);

        CategoryEntity category = saveCategory();

        ProductCreateRequest req = new ProductCreateRequest(
                "삭제상품",
                category.getId(),
                10000,
                10,
                Status.SELLING
        );

        String response =
                mockMvc.perform(post("/product")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long productId = objectMapper
                .readTree(response)
                .get("data")
                .asLong();

        mockMvc.perform(delete("/product/{productId}", productId))
                .andExpect(status().isOk());
    }
}
