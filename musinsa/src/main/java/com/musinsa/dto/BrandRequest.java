package com.musinsa.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 브랜드 생성/수정 요청.
 */
public record BrandRequest(
        @NotBlank(message = "브랜드명은 필수입니다.")
        String name
) {
}
