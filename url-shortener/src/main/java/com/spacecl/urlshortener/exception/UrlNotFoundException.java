package com.spacecl.urlshortener.exception;

/**
 * 요청한 단축 키에 해당하는 URL이 없을 때 발생하는 예외.
 */
public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String key) {
        super("존재하지 않는 단축 키입니다: " + key);
    }
}
