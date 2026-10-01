package com.musinsa;

import com.musinsa.dto.CategoryPriceRangeResponse;
import com.musinsa.dto.LowestPriceByCategoryResponse;
import com.musinsa.dto.LowestTotalBrandResponse;
import com.musinsa.service.PriceQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 무신사 과제 표준 데이터에 대한 가격 조회 API 로직 검증.
 */
@SpringBootTest
class PriceQueryServiceTest {

    @Autowired
    PriceQueryService priceQueryService;

    @Test
    @DisplayName("API1: 카테고리별 최저가격 브랜드/가격/총액 - 총액 34,100")
    void lowestPriceByCategory() {
        LowestPriceByCategoryResponse response = priceQueryService.getLowestPriceByCategory();

        assertThat(response.items()).hasSize(8);
        assertThat(response.totalPrice()).isEqualTo("34,100");

        // 첫 카테고리(상의)는 C 10,000
        LowestPriceByCategoryResponse.Item top = response.items().get(0);
        assertThat(top.category()).isEqualTo("상의");
        assertThat(top.brand()).isEqualTo("C");
        assertThat(top.price()).isEqualTo("10,000");
    }

    @Test
    @DisplayName("API2: 단일 브랜드 최저 총액 - D 브랜드 36,100")
    void lowestTotalBrand() {
        LowestTotalBrandResponse response = priceQueryService.getLowestTotalBrand();

        assertThat(response.lowest().brand()).isEqualTo("D");
        assertThat(response.lowest().totalPrice()).isEqualTo("36,100");
        assertThat(response.lowest().categories()).hasSize(8);
    }

    @Test
    @DisplayName("API3: 카테고리(상의) 최저/최고 가격 - 최저 C 10,000 / 최고 I 11,400")
    void priceRangeByCategory() {
        CategoryPriceRangeResponse response = priceQueryService.getPriceRangeByCategory("상의");

        assertThat(response.category()).isEqualTo("상의");
        assertThat(response.lowest()).extracting(CategoryPriceRangeResponse.BrandPrice::brand)
                .containsExactly("C");
        assertThat(response.lowest().get(0).price()).isEqualTo("10,000");
        assertThat(response.highest()).extracting(CategoryPriceRangeResponse.BrandPrice::brand)
                .containsExactly("I");
        assertThat(response.highest().get(0).price()).isEqualTo("11,400");
    }
}
