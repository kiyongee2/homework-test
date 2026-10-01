package com.spacecl.hotel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 공통 빈 설정.
 */
@Configuration
public class AppConfig {

    /**
     * 시스템 시계 빈. 서비스에서 주입받아 사용하므로, 테스트에서 고정 시계로
     * 교체하여 날짜 검증 로직을 결정적으로 테스트할 수 있다.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
