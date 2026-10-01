package com.example.bank.controller;

import com.example.bank.dto.*;
import com.example.bank.entity.Account;
import com.example.bank.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST 엔드포인트. 콘솔의 "메뉴 switch"가 HTTP 메서드 매핑으로 바뀝니다.
 */
@RestController
@RequestMapping("/api/accounts")   // React가 호출하는 REST 엔드포인트
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    /** 계좌 목록 → GET /accounts */
    @GetMapping
    public List<Account> getAll() {
        return service.getAllAccounts();
    }

    /** 계좌 조회 → GET /accounts/110-0001 */
    @GetMapping("/{no}")
    public Account getOne(@PathVariable String no) {
        return service.getAccount(no);
    }

    /** 계좌 개설 → POST /accounts */
    @PostMapping
    public Account create(@RequestBody CreateAccountRequest req) {
        return service.createAccount(req.owner(), req.initialBalance());
    }

    /** 입금 → POST /accounts/110-0001/deposit */
    @PostMapping("/{no}/deposit")
    public Account deposit(@PathVariable String no, @RequestBody AmountRequest req) {
        return service.deposit(no, req.amount());
    }

    /** 출금 → POST /accounts/110-0001/withdraw */
    @PostMapping("/{no}/withdraw")
    public Account withdraw(@PathVariable String no, @RequestBody AmountRequest req) {
        return service.withdraw(no, req.amount());
    }

    /** 이체 → POST /accounts/transfer */
    @PostMapping("/transfer")
    public Map<String, Object> transfer(@RequestBody TransferRequest req) {
        service.transfer(req.from(), req.to(), req.amount());
        return Map.of(
                "message", "이체 완료",
                "from", service.getAccount(req.from()),
                "to", service.getAccount(req.to()));
    }

    /** 예금주 변경 → PUT /accounts/110-0001 */
    @PutMapping("/{no}")
    public Account changeOwner(@PathVariable String no, @RequestBody OwnerRequest req) {
        return service.changeOwner(no, req.owner());
    }

    /** 계좌 해지 → DELETE /accounts/110-0001 */
    @DeleteMapping("/{no}")
    public Map<String, String> close(@PathVariable String no) {
        service.closeAccount(no);
        return Map.of("message", no + " 계좌가 해지되었습니다.");
    }
}
