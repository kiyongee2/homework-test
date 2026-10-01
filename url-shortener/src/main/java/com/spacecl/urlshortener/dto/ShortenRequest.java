package com.spacecl.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 단축 URL 생성 요청 바디.
 */
public class ShortenRequest {

    @NotBlank(message = "url 값은 필수입니다.")
    private String url;

    public ShortenRequest() {
    }

    public ShortenRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
