package com.example.bank.repository;

import com.example.bank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JpaRepository<엔티티, 기본키타입> 을 상속하면
 * save(), findAll(), findById(), delete() 등이 자동 제공됩니다.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    // 메서드 이름 규칙 → "계좌번호로 조회" 쿼리 자동 생성
    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);
}
