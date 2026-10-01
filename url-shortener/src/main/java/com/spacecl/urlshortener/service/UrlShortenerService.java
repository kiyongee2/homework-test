package com.spacecl.urlshortener.service;

import com.spacecl.urlshortener.exception.UrlNotFoundException;
import com.spacecl.urlshortener.model.ShortUrl;
import com.spacecl.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 단축 URL 핵심 비즈니스 로직.
 */
@Service
public class UrlShortenerService {

    /** 키 충돌 시 재시도 최대 횟수 */
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final UrlRepository repository;
    private final KeyGenerator keyGenerator;

    public UrlShortenerService(UrlRepository repository, KeyGenerator keyGenerator) {
        this.repository = repository;
        this.keyGenerator = keyGenerator;
    }

    /**
     * 원본 URL을 단축한다.
     *
     * <p>동일한 원본 URL을 여러 번 요청해도 매번 새로운 키를 생성한다
     * (요구사항 5). 기존에 발급된 단축 URL은 저장소에 그대로 남아 계속 동작한다.</p>
     *
     * @param originalUrl 원본 URL (http/https)
     * @return 새로 생성된 {@link ShortUrl}
     */
    public ShortUrl shorten(String originalUrl) {
        String normalized = normalize(originalUrl);

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String key = keyGenerator.generate();
            ShortUrl shortUrl = new ShortUrl(key, normalized);
            // 매우 드문 키 충돌을 저장소 수준에서 원자적으로 방지한다.
            if (repository.saveIfAbsent(shortUrl)) {
                return shortUrl;
            }
        }
        throw new IllegalStateException(
                "고유한 단축 키 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.");
    }

    /**
     * 단축 키로 원본 URL을 조회하고, 클릭 수를 1 증가시킨다 (요구사항 6).
     *
     * @param key 단축 키
     * @return 원본 URL
     * @throws UrlNotFoundException 키가 존재하지 않을 때
     */
    public String resolveAndCount(String key) {
        ShortUrl shortUrl = repository.findByKey(key)
                .orElseThrow(() -> new UrlNotFoundException(key));
        shortUrl.increaseClickCount();
        return shortUrl.getOriginalUrl();
    }

    /**
     * 단축 키의 통계 정보를 조회한다 (클릭 수는 증가시키지 않음).
     *
     * @throws UrlNotFoundException 키가 존재하지 않을 때
     */
    public ShortUrl getStats(String key) {
        return repository.findByKey(key)
                .orElseThrow(() -> new UrlNotFoundException(key));
    }

    /** 저장된 모든 단축 URL을 반환한다. */
    public Collection<ShortUrl> getAll() {
        return repository.findAll();
    }

    /**
     * URL 유효성 검증 및 정규화.
     *
     * <p>스킴(http/https)이 없으면 https:// 를 붙여 준다.</p>
     */
    private String normalize(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL은 비어 있을 수 없습니다.");
        }
        String trimmed = url.trim();
        String candidate = trimmed;
        if (!candidate.matches("^(?i)https?://.*")) {
            candidate = "https://" + candidate;
        }

        try {
            java.net.URI uri = java.net.URI.create(candidate);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("http 또는 https URL만 지원합니다: " + trimmed);
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("올바른 호스트가 포함된 URL이 아닙니다: " + trimmed);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("올바른 URL 형식이 아닙니다: " + trimmed);
        }
        return candidate;
    }
}
