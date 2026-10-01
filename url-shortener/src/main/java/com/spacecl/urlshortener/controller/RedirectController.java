package com.spacecl.urlshortener.controller;

import com.spacecl.urlshortener.service.KeyGenerator;
import com.spacecl.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 단축 키 리다이렉트 처리 (요구사항 4, 6).
 *
 * <p>GET /{key} 요청이 오면 원본 URL로 302 리다이렉트하고 클릭 수를 증가시킨다.
 * 8글자 Base62 키 패턴에만 매칭되도록 정규식을 제한하여, 정적 리소스나
 * /api 등 다른 경로와 충돌하지 않게 한다.</p>
 */
@RestController
public class RedirectController {

    private static final String KEY_PATTERN = "{key:[0-9a-zA-Z]{" + KeyGenerator.KEY_LENGTH + "}}";

    private final UrlShortenerService service;

    public RedirectController(UrlShortenerService service) {
        this.service = service;
    }

    @GetMapping("/" + KEY_PATTERN)
    public void redirect(@PathVariable String key, HttpServletResponse response) throws IOException {
        String originalUrl = service.resolveAndCount(key);
        response.setStatus(HttpServletResponse.SC_FOUND); // 302
        response.setHeader("Location", originalUrl);
    }
}
