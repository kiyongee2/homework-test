package com.musinsa;

import com.musinsa.dto.BrandRequest;
import com.musinsa.dto.BrandResponse;
import com.musinsa.dto.ProductRequest;
import com.musinsa.dto.ProductResponse;
import com.musinsa.dto.ProductUpdateRequest;
import com.musinsa.exception.BrandNotFoundException;
import com.musinsa.exception.DuplicateBrandException;
import com.musinsa.service.BrandService;
import com.musinsa.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 브랜드/상품 CRUD(API4) 검증. 각 테스트는 트랜잭션 롤백되어 데이터를 오염시키지 않는다.
 */
@SpringBootTest
@Transactional
class CrudServiceTest {

    @Autowired
    BrandService brandService;

    @Autowired
    ProductService productService;

    @Test
    @DisplayName("브랜드 생성/수정/삭제")
    void brandCrud() {
        BrandResponse created = brandService.create(new BrandRequest("Z"));
        assertThat(created.id()).isNotNull();
        assertThat(created.name()).isEqualTo("Z");

        BrandResponse updated = brandService.update(created.id(), new BrandRequest("ZZ"));
        assertThat(updated.name()).isEqualTo("ZZ");

        brandService.delete(created.id());
        assertThatThrownBy(() -> brandService.getById(created.id()))
                .isInstanceOf(BrandNotFoundException.class);
    }

    @Test
    @DisplayName("중복 브랜드명 등록 시 예외")
    void duplicateBrand() {
        assertThatThrownBy(() -> brandService.create(new BrandRequest("A")))
                .isInstanceOf(DuplicateBrandException.class);
    }

    @Test
    @DisplayName("상품 생성/수정/삭제")
    void productCrud() {
        BrandResponse brand = brandService.create(new BrandRequest("TestBrand"));

        ProductResponse created = productService.create(
                new ProductRequest(brand.id(), "상의", 12345));
        assertThat(created.price()).isEqualTo(12345);
        assertThat(created.categoryName()).isEqualTo("상의");

        ProductResponse updated = productService.update(
                created.id(), new ProductUpdateRequest(9999, "바지"));
        assertThat(updated.price()).isEqualTo(9999);
        assertThat(updated.categoryName()).isEqualTo("바지");

        productService.delete(created.id());
    }
}
