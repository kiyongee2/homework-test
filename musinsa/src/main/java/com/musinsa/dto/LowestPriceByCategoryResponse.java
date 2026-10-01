package com.musinsa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * [API 1] 카테고리별 최저가격 브랜드와 상품가격, 총액 조회 응답.
 * <pre>
 * {
 *   "카테고리": [ { "카테고리": "상의", "브랜드": "C", "가격": "10,000" }, ... ],
 *   "총액": "34,100"
 * }
 * </pre>
 */
@JsonPropertyOrder({"카테고리", "총액"})
public record LowestPriceByCategoryResponse(
        @JsonProperty("카테고리") List<Item> items,
        @JsonProperty("총액") String totalPrice
) {
    @JsonPropertyOrder({"카테고리", "브랜드", "가격"})
    public record Item(
            @JsonProperty("카테고리") String category,
            @JsonProperty("브랜드") String brand,
            @JsonProperty("가격") String price
    ) {
    }
}
