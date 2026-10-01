package com.musinsa.dto;

import com.musinsa.domain.Product;

/**
 * 상품 응답.
 */
public record ProductResponse(
        Long id,
        Long brandId,
        String brandName,
        String categoryName,
        int price
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getBrand().getId(),
                product.getBrand().getName(),
                product.getCategory().getName(),
                product.getPrice()
        );
    }
}
