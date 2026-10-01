package com.spacecl.urlshortener.dto;

import com.spacecl.urlshortener.model.ShortUrl;

import java.time.Instant;

/**
 * 단축 URL 정보 응답 바디.
 *
 * @param key         단축 키 (8글자)
 * @param shortUrl    전체 단축 URL (예: http://localhost:8080/3onGWak1)
 * @param originalUrl 원본 URL
 * @param clickCount  리다이렉트(방문) 횟수
 * @param createdAt   생성 시각
 */
public record UrlResponse(
        String key,
        String shortUrl,
        String originalUrl,
        long clickCount,
        Instant createdAt
) {
    public static UrlResponse from(ShortUrl shortUrl, String baseUrl) {
        return new UrlResponse(
                shortUrl.getKey(),
                baseUrl + "/" + shortUrl.getKey(),
                shortUrl.getOriginalUrl(),
                shortUrl.getClickCount(),
                shortUrl.getCreatedAt()
        );
    }
}
