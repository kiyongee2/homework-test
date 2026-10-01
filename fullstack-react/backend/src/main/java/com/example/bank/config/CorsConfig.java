package com.example.bank.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 설정.
 *
 * [학습 포인트]
 * - 프론트엔드(React, http://localhost:5173)와 백엔드(Spring, http://localhost:8080)는
 *   포트가 달라 브라우저가 "다른 출처(cross-origin)"로 보고 요청을 막는다.
 * - 백엔드에서 프론트 주소를 허용해 줘야 브라우저가 응답을 받을 수 있다.
 * - (개발 중에는 Vite 프록시를 써서 CORS 없이도 되지만, 직접 호출까지 대비해 열어 둔다.)
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")   // Vite 개발 서버 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
