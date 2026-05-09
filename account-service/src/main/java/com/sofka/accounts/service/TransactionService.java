package com.sofka.accounts.service;

import com.sofka.accounts.dto.TransactionReportResponse;
import com.sofka.accounts.dto.TransactionRequest;
import com.sofka.accounts.dto.TransactionResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    List<TransactionResponse> findAll();
    Mono<List<TransactionReportResponse>> findByClientAndDates(Long clientId, LocalDate startDate, LocalDate endDate);
    TransactionResponse create(Long accountId, TransactionRequest request);
}
