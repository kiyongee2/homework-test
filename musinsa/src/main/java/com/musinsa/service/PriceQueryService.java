package com.musinsa.service;

import com.musinsa.domain.Category;
import com.musinsa.domain.Product;
import com.musinsa.dto.CategoryPriceRangeResponse;
import com.musinsa.dto.LowestPriceByCategoryResponse;
import com.musinsa.dto.LowestTotalBrandResponse;
import com.musinsa.exception.CategoryNotFoundException;
import com.musinsa.repository.CategoryRepository;
import com.musinsa.repository.ProductRepository;
import com.musinsa.util.PriceFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 가격 관련 조회 API 3종의 비즈니스 로직.
 * <ul>
 *   <li>API 1: 카테고리별 최저가격 브랜드와 상품가격, 총액</li>
 *   <li>API 2: 단일 브랜드로 전 카테고리 구매 시 최저 총액 브랜드</li>
 *   <li>API 3: 카테고리 이름으로 최저/최고 가격 브랜드</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class PriceQueryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public PriceQueryService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * [API 1] 카테고리별로 최저가격인 브랜드와 그 가격을 구하고, 최저가 상품들의 총액을 계산한다.
     * 동일 최저가가 여러 브랜드일 경우 브랜드명 오름차순으로 하나를 선택한다.
     * 카테고리 표기 순서는 카테고리 등록 순서(id)를 따른다.
     */
    public LowestPriceByCategoryResponse getLowestPriceByCategory() {
        List<Product> products = productRepository.findAllWithBrandAndCategory();

        // 카테고리 id 순으로 그룹핑 (등록 순서 유지)
        Map<Category, List<Product>> byCategory = products.stream()
                .collect(Collectors.groupingBy(
                        Product::getCategory,
                        () -> new LinkedHashMap<>(),
                        Collectors.toList()));

        List<Category> orderedCategories = byCategory.keySet().stream()
                .sorted(Comparator.comparing(Category::getId))
                .toList();

        long total = 0;
        List<LowestPriceByCategoryResponse.Item> items = new java.util.ArrayList<>();
        for (Category category : orderedCategories) {
            Product cheapest = byCategory.get(category).stream()
                    .min(Comparator.comparingInt(Product::getPrice)
                            .thenComparing(p -> p.getBrand().getName()))
                    .orElseThrow();
            total += cheapest.getPrice();
            items.add(new LowestPriceByCategoryResponse.Item(
                    category.getName(),
                    cheapest.getBrand().getName(),
                    PriceFormatter.format(cheapest.getPrice())));
        }

        return new LowestPriceByCategoryResponse(items, PriceFormatter.format(total));
    }

    /**
     * [API 2] 모든 카테고리 상품을 단일 브랜드에서 구매할 때 총액이 최저인 브랜드를 찾는다.
     * 모든 카테고리를 취급하는 브랜드만 후보가 된다.
     */
    public LowestTotalBrandResponse getLowestTotalBrand() {
        List<Product> products = productRepository.findAllWithBrandAndCategory();

        long categoryCount = products.stream()
                .map(p -> p.getCategory().getId())
                .distinct()
                .count();

        // 브랜드 -> (카테고리 -> 해당 브랜드 최저가)
        Map<String, Map<Category, Integer>> brandCategoryMinPrice = new LinkedHashMap<>();
        for (Product p : products) {
            brandCategoryMinPrice
                    .computeIfAbsent(p.getBrand().getName(), k -> new LinkedHashMap<>())
                    .merge(p.getCategory(), p.getPrice(), Math::min);
        }

        String bestBrand = null;
        long bestTotal = Long.MAX_VALUE;
        Map<Category, Integer> bestPrices = null;

        // 브랜드명 오름차순으로 순회하여 동점 시 결정적으로 선택
        List<String> brands = brandCategoryMinPrice.keySet().stream().sorted().toList();
        for (String brand : brands) {
            Map<Category, Integer> categoryPrices = brandCategoryMinPrice.get(brand);
            if (categoryPrices.size() < categoryCount) {
                continue; // 모든 카테고리를 취급하지 않는 브랜드는 제외
            }
            long total = categoryPrices.values().stream().mapToLong(Integer::longValue).sum();
            if (total < bestTotal) {
                bestTotal = total;
                bestBrand = brand;
                bestPrices = categoryPrices;
            }
        }

        if (bestBrand == null) {
            // 모든 카테고리를 취급하는 브랜드가 없는 경우
            return new LowestTotalBrandResponse(
                    new LowestTotalBrandResponse.Detail(null, List.of(), PriceFormatter.format(0)));
        }

        List<LowestTotalBrandResponse.CategoryPrice> categoryPrices = bestPrices.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Category::getId)))
                .map(e -> new LowestTotalBrandResponse.CategoryPrice(
                        e.getKey().getName(), PriceFormatter.format(e.getValue())))
                .toList();

        return new LowestTotalBrandResponse(
                new LowestTotalBrandResponse.Detail(
                        bestBrand, categoryPrices, PriceFormatter.format(bestTotal)));
    }

    /**
     * [API 3] 카테고리 이름으로 최저/최고 가격 브랜드와 가격을 조회한다.
     * 동일 최저가(또는 최고가)가 여러 브랜드이면 모두 반환한다.
     */
    public CategoryPriceRangeResponse getPriceRangeByCategory(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new CategoryNotFoundException(categoryName));

        List<Product> products = productRepository.findByCategoryWithBrand(category);
        if (products.isEmpty()) {
            return new CategoryPriceRangeResponse(categoryName, List.of(), List.of());
        }

        int min = products.stream().mapToInt(Product::getPrice).min().orElseThrow();
        int max = products.stream().mapToInt(Product::getPrice).max().orElseThrow();

        List<CategoryPriceRangeResponse.BrandPrice> lowest = toBrandPrices(products, min);
        List<CategoryPriceRangeResponse.BrandPrice> highest = toBrandPrices(products, max);

        return new CategoryPriceRangeResponse(categoryName, lowest, highest);
    }

    private List<CategoryPriceRangeResponse.BrandPrice> toBrandPrices(List<Product> products, int price) {
        return products.stream()
                .filter(p -> p.getPrice() == price)
                .sorted(Comparator.comparing(p -> p.getBrand().getName()))
                .map(p -> new CategoryPriceRangeResponse.BrandPrice(
                        p.getBrand().getName(), PriceFormatter.format(p.getPrice())))
                .toList();
    }
}
