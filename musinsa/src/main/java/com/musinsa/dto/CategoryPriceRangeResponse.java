package com.musinsa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * [API 3] 카테고리 이름으로 최저, 최고 가격 브랜드와 상품 가격 조회 응답.
 * <pre>
 * {
 *   "카테고리": "상의",
 *   "최저가": [ { "브랜드": "C", "가격": "10,000" } ],
 *   "최고가": [ { "브랜드": "I", "가격": "11,400" } ]
 * }
 * </pre>
 */
@JsonPropertyOrder({"카테고리", "최저가", "최고가"})
public record CategoryPriceRangeResponse(
        @JsonProperty("카테고리") String category,
        @JsonProperty("최저가") List<BrandPrice> lowest,
        @JsonProperty("최고가") List<BrandPrice> highest
) {
    @JsonPropertyOrder({"브랜드", "가격"})
    public record BrandPrice(
            @JsonProperty("브랜드") String brand,
            @JsonProperty("가격") String price
    ) {
    }
}
