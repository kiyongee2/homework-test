package com.musinsa.controller;

import com.musinsa.dto.CategoryPriceRangeResponse;
import com.musinsa.dto.LowestPriceByCategoryResponse;
import com.musinsa.dto.LowestTotalBrandResponse;
import com.musinsa.service.PriceQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 가격 조회 API (무신사 과제 1~3번).
 */
@RestController
public class PriceQueryController {

    private final PriceQueryService priceQueryService;

    public PriceQueryController(PriceQueryService priceQueryService) {
        this.priceQueryService = priceQueryService;
    }

    /** [API 1] 카테고리별 최저가격 브랜드와 상품가격, 총액 조회 */
    @GetMapping("/api/categories/lowest-prices")
    public LowestPriceByCategoryResponse getLowestPriceByCategory() {
        return priceQueryService.getLowestPriceByCategory();
    }

    /** [API 2] 단일 브랜드로 모든 카테고리 구매 시 최저 총액 브랜드 조회 */
    @GetMapping("/api/brands/lowest-total")
    public LowestTotalBrandResponse getLowestTotalBrand() {
        return priceQueryService.getLowestTotalBrand();
    }

    /** [API 3] 카테고리 이름으로 최저/최고 가격 브랜드 조회 */
    @GetMapping("/api/categories/{categoryName}/price-range")
    public CategoryPriceRangeResponse getPriceRange(@PathVariable String categoryName) {
        return priceQueryService.getPriceRangeByCategory(categoryName);
    }
}
