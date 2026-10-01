package com.musinsa.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 상품 수정 요청. 가격은 필수, 카테고리명은 변경 시에만 지정.
 */
public record ProductUpdateRequest(
        @NotNull(message = "price는 필수입니다.")
        @Positive(message = "price는 0보다 커야 합니다.")
        Integer price,

        String categoryName
) {
}
