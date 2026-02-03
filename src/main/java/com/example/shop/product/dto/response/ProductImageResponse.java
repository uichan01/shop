package com.example.shop.product.dto.response;

import com.example.shop.product.domain.ProductImageEntity;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImageResponse {

    private Long id;
    private String photoPath;
    private int sortOrder;

    public static ProductImageResponse EntityToDto(ProductImageEntity entity) {
        return ProductImageResponse.builder()
                .id(entity.getId())
                .photoPath(entity.getPhotoPath())
                .sortOrder(entity.getSortOrder())
                .build();
    }
}
