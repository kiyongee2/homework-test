package com.musinsa.exception;

public class ProductNotFoundException extends NotFoundException {

    public ProductNotFoundException(Long productId) {
        super("상품을 찾을 수 없습니다. id=" + productId);
    }
}
