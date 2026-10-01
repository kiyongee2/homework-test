package com.spacecl.urlshortener.controller;

import com.spacecl.urlshortener.dto.ShortenRequest;
import com.spacecl.urlshortener.dto.UrlResponse;
import com.spacecl.urlshortener.model.ShortUrl;
import com.spacecl.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Comparator;
import java.util.List;

/**
 * 단축 URL REST API.
 *
 * <ul>
 *   <li>POST /api/shorten       : URL 단축</li>
 *   <li>GET  /api/stats/{key}   : 특정 단축 URL의 통계(클릭 수 등) 조회</li>
 *   <li>GET  /api/urls          : 전체 단축 URL 목록 조회</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class UrlApiController {

    private final UrlShortenerService service;

    public UrlApiController(UrlShortenerService service) {
        this.service = service;
    }

    /** URL 단축 (요구사항 1, 2, 5). */
    @PostMapping("/shorten")
    public ResponseEntity<UrlResponse> shorten(@Valid @RequestBody ShortenRequest request,
                                               HttpServletRequest httpRequest) {
        ShortUrl shortUrl = service.shorten(request.getUrl());
        UrlResponse body = UrlResponse.from(shortUrl, baseUrl(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /** 단축 URL 통계 조회 (요구사항 6). */
    @GetMapping("/stats/{key}")
    public UrlResponse stats(@PathVariable String key, HttpServletRequest httpRequest) {
        ShortUrl shortUrl = service.getStats(key);
        return UrlResponse.from(shortUrl, baseUrl(httpRequest));
    }

    /** 전체 목록 (UI 표시에 사용). 최근 생성순 정렬. */
    @GetMapping("/urls")
    public List<UrlResponse> list(HttpServletRequest httpRequest) {
        String base = baseUrl(httpRequest);
        return service.getAll().stream()
                .sorted(Comparator.comparing(ShortUrl::getCreatedAt).reversed())
                .map(s -> UrlResponse.from(s, base))
                .toList();
    }

    /** 현재 요청 기준의 서비스 기본 URL(scheme://host[:port])을 만든다. */
    private String baseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
    }
}
