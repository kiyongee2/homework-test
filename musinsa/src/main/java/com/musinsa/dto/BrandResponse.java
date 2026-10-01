package com.musinsa.dto;

import com.musinsa.domain.Brand;

/**
 * 브랜드 응답.
 */
public record BrandResponse(
        Long id,
        String name
) {
    public static BrandResponse from(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getName());
    }
}
