package com.example.bank.config;

import com.example.bank.service.AccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 서버가 켜질 때 샘플 계좌 2개를 자동으로 만들어 둡니다.
 * (H2는 메모리 DB라 서버를 끄면 사라지므로, 매번 실행 직후 바로 테스트할 수 있게 함)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final AccountService service;

    public DataInitializer(AccountService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        service.createAccount("홍길동", 100000); // 110-0001
        service.createAccount("김철수", 50000);  // 110-0002
        System.out.println(">>> 샘플 계좌 생성 완료: 110-0001(홍길동), 110-0002(김철수)");
    }
}
