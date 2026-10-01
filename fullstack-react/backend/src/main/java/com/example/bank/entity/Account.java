package com.example.bank.entity;

import jakarta.persistence.*;

/**
 * 계좌 엔티티 — DB의 account 테이블과 매핑됩니다.
 * 입금/출금 규칙(캡슐화)은 엔티티가 스스로 검사합니다.
 */
@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 자동 증가
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber; // 계좌번호 (중복 불가)

    private String owner;   // 예금주
    private long balance;   // 잔액

    protected Account() {}  // JPA가 요구하는 기본 생성자

    public Account(String accountNumber, String owner, long initialBalance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = initialBalance;
    }

    /** 입금 — 0보다 큰 금액만 허용 */
    public void deposit(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("입금액은 0보다 커야 합니다.");
        }
        this.balance += amount;
    }

    /** 출금 — 잔액 부족 시 예외 */
    public void withdraw(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("출금액은 0보다 커야 합니다.");
        }
        if (amount > this.balance) {
            throw new IllegalStateException("잔액이 부족합니다. (현재 잔액: " + this.balance + "원)");
        }
        this.balance -= amount;
    }

    /** 예금주 변경 */
    public void changeOwner(String owner) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("예금주 이름을 입력해야 합니다.");
        }
        this.owner = owner;
    }

    // Getter (JSON 응답 변환에 사용)
    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getOwner() { return owner; }
    public long getBalance() { return balance; }
}
