package com.example.bank.dto;

/** 이체 요청 : {"from":"110-0001", "to":"110-0002", "amount":30000} */
public record TransferRequest(String from, String to, long amount) {}
