package com.example.bank.service;

import com.example.bank.entity.Account;
import com.example.bank.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 은행 업무 로직 계층. @Transactional 로 DB 작업을 안전하게 묶습니다.
 */
@Service
public class AccountService {

    private final AccountRepository repository;

    // 생성자 주입(DI)
    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    /** 계좌 개설 — 계좌번호 자동 생성 후 저장 */
    @Transactional
    public Account createAccount(String owner, long initialBalance) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("예금주 이름을 입력해야 합니다.");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("초기 잔액은 0원 이상이어야 합니다.");
        }
        String accountNumber = generateAccountNumber();
        return repository.save(new Account(accountNumber, owner, initialBalance));
    }

    // 110-0001 형태의 계좌번호 생성 (중복 시 다음 번호)
    private String generateAccountNumber() {
        long seq = repository.count() + 1;
        String no = String.format("110-%04d", seq);
        while (repository.existsByAccountNumber(no)) {
            seq++;
            no = String.format("110-%04d", seq);
        }
        return no;
    }

    @Transactional(readOnly = true)
    public List<Account> getAllAccounts() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Account getAccount(String accountNumber) {
        return repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 계좌입니다: " + accountNumber));
    }

    @Transactional
    public Account deposit(String accountNumber, long amount) {
        Account account = getAccount(accountNumber);
        account.deposit(amount); // 변경 → 트랜잭션 종료 시 자동 반영(dirty checking)
        return account;
    }

    @Transactional
    public Account withdraw(String accountNumber, long amount) {
        Account account = getAccount(accountNumber);
        account.withdraw(amount);
        return account;
    }

    /** 이체 — 출금·입금을 하나의 트랜잭션으로 (실패 시 전체 롤백) */
    @Transactional
    public void transfer(String from, String to, long amount) {
        if (from.equals(to)) {
            throw new IllegalArgumentException("같은 계좌로는 이체할 수 없습니다.");
        }
        Account fromAcc = getAccount(from);
        Account toAcc = getAccount(to);
        fromAcc.withdraw(amount); // 실패하면 예외 → 입금 실행 안 되고 롤백
        toAcc.deposit(amount);
    }

    @Transactional
    public Account changeOwner(String accountNumber, String newOwner) {
        Account account = getAccount(accountNumber);
        account.changeOwner(newOwner);
        return account;
    }

    @Transactional
    public void closeAccount(String accountNumber) {
        Account account = getAccount(accountNumber);
        repository.delete(account);
    }
}
