package com.musinsa.exception;

public class BrandNotFoundException extends NotFoundException {

    public BrandNotFoundException(Long brandId) {
        super("브랜드를 찾을 수 없습니다. id=" + brandId);
    }

    public BrandNotFoundException(String name) {
        super("브랜드를 찾을 수 없습니다. name=" + name);
    }
}
