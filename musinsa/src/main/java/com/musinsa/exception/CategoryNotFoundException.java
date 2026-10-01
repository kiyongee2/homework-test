package com.musinsa.exception;

public class CategoryNotFoundException extends NotFoundException {

    public CategoryNotFoundException(String name) {
        super("카테고리를 찾을 수 없습니다. name=" + name);
    }
}
