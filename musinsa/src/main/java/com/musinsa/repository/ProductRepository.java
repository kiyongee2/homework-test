package com.musinsa.repository;

import com.musinsa.domain.Brand;
import com.musinsa.domain.Category;
import com.musinsa.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /** 모든 상품을 브랜드/카테고리와 함께 조회 (open-in-view=false 이므로 fetch join) */
    @Query("select p from Product p join fetch p.brand join fetch p.category")
    List<Product> findAllWithBrandAndCategory();

    /** 특정 카테고리의 상품을 브랜드와 함께 조회 */
    @Query("select p from Product p join fetch p.brand join fetch p.category c where c = :category")
    List<Product> findByCategoryWithBrand(@Param("category") Category category);

    List<Product> findByBrand(Brand brand);

    void deleteByBrand(Brand brand);
}
