package com.example.shop.product.controller;

import com.example.shop.category.domain.CategoryEntity;
import com.example.shop.category.repository.CategoryRepository;
import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.domain.Role;
import com.example.shop.member.repository.MemberRepository;
import com.example.shop.product.domain.ProductEntity;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import java.util.List;

import static org.assertj.core.api.Assertions.*;
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
                ,"테스트 상품 설명"
        );


        MockMultipartFile productPart =
                new MockMultipartFile(
                        "product",
                        "",
                        "application/json",
                        objectMapper.writeValueAsBytes(req)
                );

        mockMvc.perform(multipart("/product")
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("상품 등록 실패 테스트 (USER, 권한X)")
    void registerProductFailByUser() throws Exception {
        MemberEntity user = saveTestMember();
        setSecurityContext(user);

        ProductCreateRequest req = new ProductCreateRequest(
                "테스트상품",
                null,
                10000,
                10,
                Status.SELLING
                ,"테스트 상품 설명"
        );


        MockMultipartFile productPart =
                new MockMultipartFile(
                        "product",
                        "",
                        "application/json",
                        objectMapper.writeValueAsBytes(req)
                );

        mockMvc.perform(multipart("/product")
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
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
                ,"테스트 상품 설명"
        );


        MockMultipartFile productPart =
                new MockMultipartFile(
                        "product",
                        "",
                        "application/json",
                        objectMapper.writeValueAsBytes(req)
                );

        String response =
                mockMvc.perform(multipart("/product")
                                .file(productPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
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
    @DisplayName("상품 단건 조회 실패(상품 존재X)")
    void getProductNotFound() throws Exception {
        MemberEntity member = saveTestMember();
        setSecurityContext(member);

        mockMvc.perform(get("/product/{productId}", 9999L))
                .andExpect(status().isBadRequest());
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
    @DisplayName("상품 목록 조회 - 카테고리 검색")
    void getProductList_WithKeyword() throws Exception {
        MemberEntity seller = saveSeller();
        setSecurityContext(seller);

        CategoryEntity category1 = CategoryEntity.builder().name("전자기기").build();
        CategoryEntity savedCategory1 = categoryRepository.save(category1);
        CategoryEntity category2 = CategoryEntity.builder().name("노트북").build();
        category2.setCategoryEntity(savedCategory1); //상위분류 설정
        CategoryEntity savedCategory2 = categoryRepository.save(category2);

        ProductEntity p1 = ProductEntity.builder()
                .seller(seller)
                .category(savedCategory1) //분류: 전자기기
                .name("일반 전자기기")
                .price(1000000)
                .stock(10)
                .status(Status.SELLING)
                .build();

        ProductEntity p2 = ProductEntity.builder()
                .seller(seller)
                .category(savedCategory2) //분류: 전자기기-노트북
                .name("게이밍 노트북")
                .price(1010000)
                .stock(10)
                .status(Status.SELLING)
                .build();

        productRepository.saveAll(List.of(p1, p2));


       mockMvc.perform(get("/product")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", savedCategory1.getId().toString())) //전자기기
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();//p1, p2


        mockMvc.perform(get("/product")
                        .param("page", "0")
                        .param("size", "10")
                        .param("categoryId", savedCategory2.getId().toString())) //전자기기-노트북
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1)); //p2
    }


    @Test
    @DisplayName("상품 삭제 테스트 (SELLER)")
    void deleteProduct() throws Exception {
        MemberEntity seller = saveSeller();
        setSecurityContext(seller);

        CategoryEntity category = saveCategory();

        ProductCreateRequest req = new ProductCreateRequest(
                "테스트상품",
                null,
                10000,
                10,
                Status.SELLING
                ,"테스트 상품 설명"
        );


        MockMultipartFile productPart =
                new MockMultipartFile(
                        "product",
                        "",
                        "application/json",
                        objectMapper.writeValueAsBytes(req)
                );

        String response =
                mockMvc.perform(multipart("/product")
                                .file(productPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
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

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow();

        assertThat(product.getStatus()).isEqualTo(Status.DELETED);
    }
}
