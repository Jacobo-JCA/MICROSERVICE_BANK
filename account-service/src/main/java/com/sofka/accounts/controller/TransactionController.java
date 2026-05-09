package com.sofka.accounts.controller;

import com.sofka.accounts.dto.TransactionReportResponse;
import com.sofka.accounts.dto.TransactionRequest;
import com.sofka.accounts.dto.TransactionResponse;
import com.sofka.accounts.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllMovements() {
        return ResponseEntity
                .ok(transactionService.findAll());
    }

    @GetMapping("/details")
    public Mono<ResponseEntity<List<TransactionReportResponse>>> getReport(
            @RequestParam Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return transactionService.findByClientAndDates(clientId, startDate, endDate)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{accountId}")
    public ResponseEntity<TransactionResponse> createMovement(
            @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transactionService.create(accountId, request));
    }
}