package com.spacecl.urlshortener.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 단축 URL 한 건을 표현하는 도메인 모델.
 *
 * <p>클릭 수는 여러 요청이 동시에 리다이렉트를 호출해도 정확하게 집계되도록
 * {@link AtomicLong}으로 관리한다.</p>
 */
public class ShortUrl {

    /** 단축 URL 키 (8글자). 예: "3onGWak1" */
    private final String key;

    /** 원본 URL */
    private final String originalUrl;

    /** 생성 시각 */
    private final Instant createdAt;

    /** 리다이렉트(방문) 횟수 */
    private final AtomicLong clickCount;

    public ShortUrl(String key, String originalUrl) {
        this.key = key;
        this.originalUrl = originalUrl;
        this.createdAt = Instant.now();
        this.clickCount = new AtomicLong(0);
    }

    /** 클릭 수를 1 증가시키고 증가 후 값을 반환한다. (thread-safe) */
    public long increaseClickCount() {
        return clickCount.incrementAndGet();
    }

    public String getKey() {
        return key;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getClickCount() {
        return clickCount.get();
    }
}
