package com.musinsa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * [API 2] 단일 브랜드로 모든 카테고리 상품을 구매할 때 최저가격에 판매하는
 * 브랜드와 카테고리별 상품가격, 총액 조회 응답.
 * <pre>
 * {
 *   "최저가": {
 *     "브랜드": "D",
 *     "카테고리": [ { "카테고리": "상의", "가격": "10,100" }, ... ],
 *     "총액": "36,100"
 *   }
 * }
 * </pre>
 */
public record LowestTotalBrandResponse(
        @JsonProperty("최저가") Detail lowest
) {
    @JsonPropertyOrder({"브랜드", "카테고리", "총액"})
    public record Detail(
            @JsonProperty("브랜드") String brand,
            @JsonProperty("카테고리") List<CategoryPrice> categories,
            @JsonProperty("총액") String totalPrice
    ) {
    }

    @JsonPropertyOrder({"카테고리", "가격"})
    public record CategoryPrice(
            @JsonProperty("카테고리") String category,
            @JsonProperty("가격") String price
    ) {
    }
}
