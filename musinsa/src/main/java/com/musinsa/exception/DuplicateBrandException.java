package com.musinsa.exception;

/**
 * 이미 존재하는 브랜드명을 등록하려 할 때 발생 (HTTP 409).
 */
public class DuplicateBrandException extends RuntimeException {

    public DuplicateBrandException(String name) {
        super("이미 존재하는 브랜드명입니다. name=" + name);
    }
}
