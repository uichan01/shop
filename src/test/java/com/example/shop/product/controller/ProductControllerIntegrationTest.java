package com.example.shop.product.controller;

import com.example.shop.category.domain.CategoryEntity;
import com.example.shop.category.repository.CategoryRepository;
import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.domain.Role;
import com.example.shop.member.repository.MemberRepository;
import com.example.shop.product.domain.ProductEntity;
import com.example.shop.product.domain.ProductStatus;
import com.example.shop.product.dto.request.ProductCreateRequest;
import com.example.shop.product.dto.request.ProductUpdateRequest;
import com.example.shop.product.repository.ProductRepository;
import com.example.shop.security.dto.CustomUserDetails;
import com.example.shop.security.dto.MemberDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;


import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
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

    private Authentication buildAuthentication(MemberEntity member) {
        CustomUserDetails userDetails = new CustomUserDetails(
                new MemberDto(member.getEmail(), member.getPassword(), member.getRole().name())
        );
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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
        // given: seller 로그인 상태, 카테고리 생성
        MemberEntity seller = saveSeller();
        setSecurityContext(seller);
        CategoryEntity category = saveCategory();
        ProductCreateRequest req = new ProductCreateRequest(
                "테스트상품",
                category.getId(),
                10000,
                10,
                ProductStatus.SELLING,
                "테스트 상품 설명"
        );
        MockMultipartFile productPart =
                new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(req));

        // when: POST /seller/product 상품 등록 요청
        mockMvc.perform(multipart("/seller/product")
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // then: 200 반환, 생성된 productId 존재
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("상품 등록 실패 테스트 (USER, 권한X)")
    void registerProductFailByUser() throws Exception {
        // given: ROLE_USER 로그인 상태
        MemberEntity user = saveTestMember();
        setSecurityContext(user);
        ProductCreateRequest req = new ProductCreateRequest(
                "테스트상품",
                null,
                10000,
                10,
                ProductStatus.SELLING,
                "테스트 상품 설명"
        );
        MockMultipartFile productPart =
                new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(req));

        // when: POST /seller/product 상품 등록 요청
        mockMvc.perform(multipart("/seller/product")
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // then: SecurityConfig /product POST → hasRole("SELLER") → ROLE_USER 차단 → 403
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("상품 단건 조회 테스트")
    void getProductDetail() throws Exception {
        // given: seller가 상품 등록 완료, productId 파싱
        MemberEntity member = saveSeller();
        setSecurityContext(member);
        ProductCreateRequest req = new ProductCreateRequest(
                "테스트상품",
                null,
                10000,
                10,
                ProductStatus.SELLING,
                "테스트 상품 설명"
        );
        MockMultipartFile productPart =
                new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(req));
        String response =
                mockMvc.perform(multipart("/seller/product")
                                .file(productPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
        Long productId = objectMapper.readTree(response).get("data").asLong();

        // when: GET /product/{productId} 단건 조회 요청
        mockMvc.perform(get("/product/{productId}", productId))
                // then: 200 반환, 등록한 상품명 일치 확인
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("테스트상품"));
    }

    @Test
    @DisplayName("상품 단건 조회 실패(상품 존재X)")
    void getProductNotFound() throws Exception {
        // given: 존재하지 않는 상품 ID
        MemberEntity member = saveTestMember();
        setSecurityContext(member);

        // when: GET /product/9999 존재하지 않는 상품 조회
        mockMvc.perform(get("/product/{productId}", 9999L))
                // then: NoSuchElementException → GlobalExceptionHandler → 400
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("상품 목록 조회 테스트")
    void getProductList() throws Exception {
        // given: 조건 없이 전체 목록 조회
        MemberEntity member = saveTestMember();
        setSecurityContext(member);

        // when: GET /product?page=0&size=10 목록 조회 요청
        mockMvc.perform(get("/product")
                        .param("page", "0")
                        .param("size", "10"))
                // then: 200 반환, 응답 data가 배열
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("상품 목록 조회 - 카테고리 검색")
    void getProductList_WithKeyword() throws Exception {
        // given: 상위 카테고리(전자기기) - 하위 카테고리(노트북) 구조, 각 카테고리에 상품 등록
        MemberEntity seller = saveSeller();
        setSecurityContext(seller);
        CategoryEntity category1 = CategoryEntity.builder().name("전자기기").build();
        CategoryEntity savedCategory1 = categoryRepository.save(category1);
        CategoryEntity category2 = CategoryEntity.builder().name("노트북").build();
        category2.setCategoryEntity(savedCategory1); //상위분류 설정
        CategoryEntity savedCategory2 = categoryRepository.save(category2);
        ProductEntity p1 = ProductEntity.builder()
                .seller(seller).category(savedCategory1).name("일반 전자기기") //분류: 전자기기
                .price(1000000).stock(10).status(ProductStatus.SELLING).build();
        ProductEntity p2 = ProductEntity.builder()
                .seller(seller).category(savedCategory2).name("게이밍 노트북") //분류: 전자기기-노트북
                .price(1010000).stock(10).status(ProductStatus.SELLING).build();
        productRepository.saveAll(List.of(p1, p2));

        // when: categoryId=전자기기 로 조회 → 하위 카테고리 포함 전체 조회
        mockMvc.perform(get("/product")
                        .param("page", "0").param("size", "10")
                        .param("categoryId", savedCategory1.getId().toString()))
                // then: p1, p2 모두 조회 (2건)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        // when: categoryId=노트북 으로 조회
        mockMvc.perform(get("/product")
                        .param("page", "0").param("size", "10")
                        .param("categoryId", savedCategory2.getId().toString()))
                // then: p2만 조회 (1건)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }


    @Test
    @DisplayName("상품 삭제 테스트 (SELLER)")
    void deleteProduct() throws Exception {
        // given: seller가 상품 등록 완료, productId 파싱
        MemberEntity seller = saveSeller();
        setSecurityContext(seller);
        CategoryEntity category = saveCategory();
        ProductCreateRequest req = new ProductCreateRequest(
                "테스트상품",
                null,
                10000,
                10,
                ProductStatus.SELLING,
                "테스트 상품 설명"
        );
        MockMultipartFile productPart =
                new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(req));
        String response =
                mockMvc.perform(multipart("/seller/product")
                                .file(productPart)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
        Long productId = objectMapper.readTree(response).get("data").asLong();

        // when: DELETE /seller/product/{productId} 삭제 요청
        mockMvc.perform(delete("/seller/product/{productId}", productId))
                // then: 200 반환
                .andExpect(status().isOk());

        // then: DB 조회 시 상태가 DELETED로 변경됨 (실제 삭제가 아닌 soft delete)
        ProductEntity product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DELETED);
    }

    @Test
    @DisplayName("상품 수정 성공 (SELLER, 본인 상품)")
    void updateProduct() throws Exception {
        // given: seller가 상품 등록 완료
        MemberEntity seller = saveSeller();
        setSecurityContext(seller);
        CategoryEntity category = saveCategory();
        ProductCreateRequest createReq = new ProductCreateRequest("수정전상품", category.getId(), 10000, 10, ProductStatus.SELLING, "설명");
        MockMultipartFile createPart = new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(createReq));
        String createResponse = mockMvc.perform(multipart("/seller/product").file(createPart).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long productId = objectMapper.readTree(createResponse).get("data").asLong();

        // when: 이름·가격·재고를 변경한 수정 요청
        ProductUpdateRequest updateReq = ProductUpdateRequest.builder()
                .id(productId).name("수정후상품").categoryId(category.getId())
                .price(20000).stock(5).productStatus(ProductStatus.SELLING).description("수정설명")
                .build();
        MockMultipartFile updatePart = new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(updateReq));
        mockMvc.perform(multipart("/seller/product").file(updatePart).contentType(MediaType.MULTIPART_FORM_DATA).with(req -> { req.setMethod("PUT"); return req; }))
                // then: 200 반환
                .andExpect(status().isOk());

        // then: 단건 조회 시 수정된 이름·가격 확인
        mockMvc.perform(get("/product/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정후상품"))
                .andExpect(jsonPath("$.data.price").value(20000));
    }

    @Test
    @DisplayName("상품 수정 실패 - 타인 셀러가 수정 시도")
    void updateProductByOtherSeller() throws Exception {
        // given: seller가 상품 등록, otherSeller는 해당 상품과 무관
        MemberEntity seller = saveSeller();
        MemberEntity otherSeller = memberRepository.save(
                MemberEntity.builder().name("다른셀러").email("other@test.com")
                        .password(passwordEncoder.encode("password@")).role(Role.ROLE_SELLER).build()
        );
        CategoryEntity category = saveCategory();
        ProductCreateRequest createReq = new ProductCreateRequest("원본상품", category.getId(), 10000, 10, ProductStatus.SELLING, "설명");
        MockMultipartFile createPart = new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(createReq));
        String createResponse = mockMvc.perform(multipart("/seller/product").file(createPart).contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(authentication(buildAuthentication(seller))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long productId = objectMapper.readTree(createResponse).get("data").asLong();

        // when: otherSeller 컨텍스트로 PUT /seller/product 요청
        ProductUpdateRequest updateReq = ProductUpdateRequest.builder()
                .id(productId).name("탈취상품").categoryId(category.getId())
                .price(1).stock(1).productStatus(ProductStatus.SELLING).description("x")
                .build();
        MockMultipartFile updatePart = new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(updateReq));
        mockMvc.perform(multipart("/seller/product").file(updatePart).contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .with(authentication(buildAuthentication(otherSeller))))
                // then: IllegalStateException → 400
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("상품 삭제 실패 - 타인 셀러가 삭제 시도")
    void deleteProductByOtherSeller() throws Exception {
        // given: seller가 상품 등록, otherSeller는 해당 상품과 무관
        MemberEntity seller = saveSeller();
        MemberEntity otherSeller = memberRepository.save(
                MemberEntity.builder().name("다른셀러").email("other@test.com")
                        .password(passwordEncoder.encode("password@")).role(Role.ROLE_SELLER).build()
        );
        CategoryEntity category = saveCategory();
        ProductCreateRequest createReq = new ProductCreateRequest("삭제대상상품", category.getId(), 10000, 10, ProductStatus.SELLING, "설명");
        MockMultipartFile createPart = new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(createReq));
        String createResponse = mockMvc.perform(multipart("/seller/product").file(createPart).contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(authentication(buildAuthentication(seller))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long productId = objectMapper.readTree(createResponse).get("data").asLong();

        // when: otherSeller 컨텍스트로 DELETE /seller/product/{productId} 요청
        mockMvc.perform(delete("/seller/product/{productId}", productId)
                        .with(authentication(buildAuthentication(otherSeller))))
                // then: IllegalStateException → 400
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("상품 목록 조회 - 키워드 검색")
    void getProductList_WithKeywordSearch() throws Exception {
        // given: seller가 이름이 다른 상품 2개 등록
        MemberEntity seller = saveSeller();
        setSecurityContext(seller);
        CategoryEntity category = saveCategory();
        productRepository.saveAll(List.of(
                ProductEntity.builder().seller(seller).category(category).name("애플 맥북").price(2000000).stock(5).status(ProductStatus.SELLING).build(),
                ProductEntity.builder().seller(seller).category(category).name("삼성 갤럭시").price(1000000).stock(10).status(ProductStatus.SELLING).build()
        ));

        // when: GET /product?keyword=맥북 으로 검색
        mockMvc.perform(get("/product").param("page", "0").param("size", "10").param("keyword", "맥북"))
                // then: 200 반환, 맥북 포함 상품 1건만 조회
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("애플 맥북"));
    }

    @Test
    @DisplayName("상품 목록 조회 - 셀러 ID 필터링")
    void getProductList_WithSellerFilter() throws Exception {
        // given: seller1과 seller2가 각각 상품 등록
        MemberEntity member = saveTestMember();
        setSecurityContext(member);

        MemberEntity seller1 = saveSeller();
        MemberEntity seller2 = memberRepository.save(
                MemberEntity.builder().name("셀러2").email("seller2@test.com")
                        .password(passwordEncoder.encode("password@")).role(Role.ROLE_SELLER).build()
        );
        CategoryEntity category = saveCategory();
        productRepository.saveAll(List.of(
                ProductEntity.builder().seller(seller1).category(category).name("셀러1상품").price(10000).stock(10).status(ProductStatus.SELLING).build(),
                ProductEntity.builder().seller(seller2).category(category).name("셀러2상품").price(20000).stock(5).status(ProductStatus.SELLING).build()
        ));

        // when: GET /product?SellerId={seller1.id} 로 필터링
        mockMvc.perform(get("/product").param("page", "0").param("size", "10").param("SellerId", seller1.getId().toString()))
                // then: 200 반환, seller1 상품 1건만 조회
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("셀러1상품"));
    }
}
