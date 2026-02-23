package com.example.shop.cart.service;

import com.example.shop.cart.domain.CartEntity;
import com.example.shop.cart.domain.CartItemEntity;
import com.example.shop.cart.dto.request.AddCartItemRequest;
import com.example.shop.cart.dto.request.UpdateCartItemRequest;
import com.example.shop.cart.dto.response.CartInfoResponse;
import com.example.shop.cart.dto.response.CartItemResponse;
import com.example.shop.cart.repository.CartItemRepository;
import com.example.shop.cart.repository.CartRepository;
import com.example.shop.member.domain.MemberEntity;
import com.example.shop.member.repository.MemberRepository;
import com.example.shop.product.domain.ProductEntity;
import com.example.shop.product.domain.ProductImageEntity;
import com.example.shop.product.repository.ProductImageRepository;
import com.example.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    //본인 장바구니조회
    @Override
    @Transactional
    public CartInfoResponse getMyCart(String username) {
        MemberEntity member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        CartEntity cart = cartRepository.findByMember(member)
                .orElseGet(() -> cartRepository.save(new CartEntity(member))); //없을경우 새로생성

        int totalItemTypeCount = 0; //총 상품 종류
        int totalQuantity = 0; //총 상품 개수
        int totalPrice = 0; //총 가격

        List<CartItemEntity> cartItems = cartItemRepository.findAllByCart(cart);
        List<CartItemResponse> cartItemResponses = new ArrayList<>();

        for (CartItemEntity cartItem : cartItems) {
            ProductEntity product = cartItem.getProduct();

            //대표사진 가져옴
            ProductImageEntity image = productImageRepository
                    .findFirstByProductIdOrderBySortOrderAsc(product.getId())
                    .orElse(null);

            //장바구니 요약정보
            totalItemTypeCount++;
            totalQuantity += cartItem.getQuantity();
            totalPrice += product.getPrice() * cartItem.getQuantity();

            CartItemResponse cartItemresponse = CartItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .totalPrice(product.getPrice() * cartItem.getQuantity())
                    .thumbnailUrl(image == null ? null : image.getPhotoPath())
                    .build();

            cartItemResponses.add(cartItemresponse);
        }

        CartInfoResponse response = CartInfoResponse.builder()
                .items(cartItemResponses)
                .totalItemTypeCount(totalItemTypeCount)
                .totalQuantity(totalQuantity)
                .totalPrice(totalPrice)
                .build();

        return response;
    }

    //장바구니 상품추가
    @Override
    @Transactional
    public void addProductToMyCart(String username, AddCartItemRequest request) {
        MemberEntity member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        CartEntity cart = cartRepository.findByMember(member)
                .orElseGet(() -> cartRepository.save(new CartEntity(member))); //없을경우 새로생성

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

        CartItemEntity cartItem = CartItemEntity.builder()
                .cart(cart)
                .product(product)
                .quantity(request.getQuantity())
                .build();

        cartItemRepository.save(cartItem);
    }

    //장바구니 상품삭제
    @Override
    @Transactional
    public void removeProductFromMyCart(String username, Long productId) {
        MemberEntity member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        CartEntity cart = cartRepository.findByMember(member)
                .orElseThrow(() -> new NoSuchElementException("장바구니가 존재하지 않습니다."));

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

        CartItemEntity cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new NoSuchElementException("장바구니의 상품을 찾을 수 없습니다."));

        cartItemRepository.delete(cartItem);
    }

    //장바구니 수량변경
    @Override
    @Transactional
    public void updateMyCartProductQuantity(String username, UpdateCartItemRequest request) {
        MemberEntity member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        CartEntity cart = cartRepository.findByMember(member)
                .orElseThrow(() -> new NoSuchElementException("장바구니가 존재하지 않습니다."));

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NoSuchElementException("상품을 찾을 수 없습니다."));

        CartItemEntity cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new NoSuchElementException("장바구니의 상품을 찾을 수 없습니다."));

        cartItem.updateQuantity(request.getQuantity());
    }
}
