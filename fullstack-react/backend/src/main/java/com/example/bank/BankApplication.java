package com.example.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 애플리케이션 시작점(main).
 * 실행하면 내장 톰캣 서버가 8080 포트에서 요청을 기다립니다.
 */
@SpringBootApplication
public class BankApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
        System.out.println("\n>>> 은행 서버 시작! http://localhost:8080/accounts");
        System.out.println(">>> H2 콘솔: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:bankdb)\n");
    }
}
