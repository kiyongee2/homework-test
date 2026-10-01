package com.musinsa.service;

import com.musinsa.domain.Brand;
import com.musinsa.dto.BrandRequest;
import com.musinsa.dto.BrandResponse;
import com.musinsa.exception.BrandNotFoundException;
import com.musinsa.exception.DuplicateBrandException;
import com.musinsa.repository.BrandRepository;
import com.musinsa.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [API 4] 브랜드 등록/수정/삭제/조회.
 */
@Service
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public BrandService(BrandRepository brandRepository, ProductRepository productRepository) {
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
    }

    public List<BrandResponse> getAll() {
        return brandRepository.findAll().stream()
                .map(BrandResponse::from)
                .toList();
    }

    public BrandResponse getById(Long id) {
        return BrandResponse.from(findBrand(id));
    }

    @Transactional
    public BrandResponse create(BrandRequest request) {
        if (brandRepository.existsByName(request.name())) {
            throw new DuplicateBrandException(request.name());
        }
        Brand saved = brandRepository.save(new Brand(request.name()));
        return BrandResponse.from(saved);
    }

    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = findBrand(id);
        if (!brand.getName().equals(request.name()) && brandRepository.existsByName(request.name())) {
            throw new DuplicateBrandException(request.name());
        }
        brand.changeName(request.name());
        return BrandResponse.from(brand);
    }

    @Transactional
    public void delete(Long id) {
        Brand brand = findBrand(id);
        // 브랜드 삭제 시 해당 브랜드의 상품도 함께 삭제
        productRepository.deleteByBrand(brand);
        brandRepository.delete(brand);
    }

    private Brand findBrand(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new BrandNotFoundException(id));
    }
}
