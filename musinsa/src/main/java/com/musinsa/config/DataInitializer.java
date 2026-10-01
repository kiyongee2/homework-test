package com.musinsa.config;

import com.musinsa.domain.Brand;
import com.musinsa.domain.Category;
import com.musinsa.domain.Product;
import com.musinsa.repository.BrandRepository;
import com.musinsa.repository.CategoryRepository;
import com.musinsa.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 애플리케이션 시작 시 무신사 과제 표준 데이터(브랜드 A~I, 8개 카테고리, 가격표)를 적재한다.
 */
@Configuration
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    // 카테고리 (표기 순서 = 등록 순서)
    private static final String[] CATEGORIES = {
            "상의", "아우터", "바지", "스니커즈", "가방", "모자", "양말", "액세서리"
    };

    // 브랜드
    private static final String[] BRANDS = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};

    // 가격표: 행=브랜드(A~I), 열=카테고리(상의, 아우터, 바지, 스니커즈, 가방, 모자, 양말, 액세서리)
    private static final int[][] PRICES = {
            {11200, 5500, 4200, 9000, 2000, 1700, 1800, 2300}, // A
            {10500, 5900, 3800, 9100, 2100, 2000, 2000, 2200}, // B
            {10000, 6200, 3300, 9200, 2200, 1900, 2200, 2100}, // C
            {10100, 5100, 3000, 9500, 2500, 1500, 2400, 2000}, // D
            {10700, 5000, 3800, 9900, 2300, 1800, 2100, 2100}, // E
            {11200, 7200, 4000, 9300, 2100, 1600, 2300, 1900}, // F
            {10500, 5800, 3900, 9000, 2200, 1700, 2100, 2000}, // G
            {10800, 6300, 3100, 9700, 2100, 1600, 2000, 2000}, // H
            {11400, 6700, 3200, 9500, 2400, 1700, 1700, 2400}  // I
    };

    public DataInitializer(CategoryRepository categoryRepository,
                           BrandRepository brandRepository,
                           ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return; // 이미 초기화된 경우 스킵
        }

        List<Category> categories = new ArrayList<>();
        for (String name : CATEGORIES) {
            categories.add(categoryRepository.save(new Category(name)));
        }

        List<Brand> brands = new ArrayList<>();
        for (String name : BRANDS) {
            brands.add(brandRepository.save(new Brand(name)));
        }

        List<Product> products = new ArrayList<>();
        for (int b = 0; b < BRANDS.length; b++) {
            for (int c = 0; c < CATEGORIES.length; c++) {
                products.add(new Product(brands.get(b), categories.get(c), PRICES[b][c]));
            }
        }
        productRepository.saveAll(products);
    }
}
