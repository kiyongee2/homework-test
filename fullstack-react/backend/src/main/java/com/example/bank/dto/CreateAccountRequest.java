package com.example.bank.dto;

/** 계좌 개설 요청 : {"owner":"이영희", "initialBalance":30000} */
public record CreateAccountRequest(String owner, long initialBalance) {}
