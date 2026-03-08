package com.example.shop.product.service;

import com.example.shop._common.storage.ImageStorage;
import com.example.shop.category.domain.CategoryEntity;
import com.example.shop.category.repository.CategoryRepository;
import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.domain.Role;
import com.example.shop.member.repository.MemberRepository;
import com.example.shop.product.domain.ProductEntity;
import com.example.shop.product.domain.ProductImageEntity;
import com.example.shop.product.dto.request.ProductCreateRequest;
import com.example.shop.product.dto.request.ProductUpdateRequest;
import com.example.shop.product.dto.request.SearchOptionRequest;
import com.example.shop.product.dto.response.ProductDetailResponse;
import com.example.shop.product.dto.response.ProductImageResponse;
import com.example.shop.product.dto.response.ProductListResponse;
import com.example.shop.product.mapper.ProductMapper;
import com.example.shop.product.repository.ProductImageRepository;
import com.example.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    private final ProductMapper productMapper;

    private final ImageStorage imageStorage;

    //상품등록
    @Override
    @Transactional
    public Long registerProduct(ProductCreateRequest request, String sellerName, List<MultipartFile> images) {
        MemberEntity member = memberRepository.findByEmail(sellerName)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));
        if (member.getRole() != Role.ROLE_SELLER) //권한검증
            throw new IllegalStateException("판매자만 상품 등록이 가능합니다.");

        CategoryEntity category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다."));
        }

        ProductEntity product = ProductEntity.builder()
                .name(request.getName())
                .seller(member)
                .category(category)
                .stock(request.getStock())
                .price(request.getPrice())
                .status(request.getProductStatus())
                .description(request.getDescription())
                .build();

        ProductEntity savedProduct = productRepository.save(product);

        if (images != null) {
            for (int sortOrder = 1; sortOrder <= images.size(); sortOrder++) {
                String path = imageStorage.save(images.get(sortOrder));
                ProductImageEntity image = ProductImageEntity.builder()
                        .photoUrl(path)
                        .product(savedProduct)
                        .sortOrder(sortOrder)
                        .build();

                productImageRepository.save(image);
            }
        }

        return savedProduct.getId();
    }

    @Override
    @Transactional
    public Long updateProduct(ProductUpdateRequest request, String sellerName, List<MultipartFile> images) {
        MemberEntity seller = memberRepository.findByEmail(sellerName)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        if (seller.getRole() != Role.ROLE_SELLER) {
            throw new IllegalStateException("판매자만 상품 수정이 가능합니다.");
        }

        ProductEntity product = productRepository.findById(request.getId())
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new IllegalStateException("본인이 등록한 상품만 수정할 수 있습니다.");
        }

        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다."));

        product.update(
                request.getName(),
                request.getPrice(),
                request.getStock(),
                request.getProductStatus(),
                category,
                request.getDescription()
        );

        //이미지 수정시
        if (images != null) {
            {
                //기존이미지 삭제
                productImageRepository.deleteByProductId(product.getId());
                //저장
                for (int sortOrder = 1; sortOrder <= images.size(); sortOrder++) {
                    String path = imageStorage.save(images.get(sortOrder));
                    ProductImageEntity image = ProductImageEntity.builder()
                            .photoUrl(path)
                            .product(product)
                            .sortOrder(sortOrder)
                            .build();

                    productImageRepository.save(image);
                }
            }
        }
        return product.getId();
    }


    @Override
    @Transactional
    public void deleteProduct(Long productId, String sellerName) {
        MemberEntity seller = memberRepository.findByEmail(sellerName)
                .orElseThrow(() -> new NoSuchElementException("판매자를 찾을 수 없습니다."));

        if (seller.getRole() != Role.ROLE_SELLER) {
            throw new IllegalStateException("판매자만 상품 삭제가 가능합니다.");
        }

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new IllegalStateException("본인 상품만 삭제할 수 있습니다.");
        }

        product.delete(); // 상태 변경
    }

    @Override
    public ProductDetailResponse getProduct(Long productId) {
        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(()-> new NoSuchElementException("상품을 찾을 수 없습니다."));

        List<ProductImageEntity> images = productImageRepository.findByProductIdOrderBySortOrderAsc(productId);

        List<ProductImageResponse> imageResponses = images.stream()
                        .map(ProductImageResponse::EntityToDto)
                        .toList();

        ProductDetailResponse response = ProductDetailResponse.builder()
                .productId(productEntity.getId())
                .name(productEntity.getName())
                .price(productEntity.getPrice())
                .description(productEntity.getDescription())
                .productStatus(productEntity.getStatus())
                .images(imageResponses)
                .build();

        return response;
    }

    @Override
    public List<ProductListResponse> getProducts(SearchOptionRequest searchOption, int page, int size) {

        int offset = page * size;
        List<ProductListResponse> resultList = productMapper.findProductListWithOption(offset, size, searchOption);
        return resultList;
    }
}
