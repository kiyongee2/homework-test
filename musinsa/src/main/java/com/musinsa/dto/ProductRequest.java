package com.musinsa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 상품 생성 요청.
 */
public record ProductRequest(
        @NotNull(message = "brandId는 필수입니다.")
        Long brandId,

        @NotBlank(message = "categoryName은 필수입니다.")
        String categoryName,

        @NotNull(message = "price는 필수입니다.")
        @Positive(message = "price는 0보다 커야 합니다.")
        Integer price
) {
}
