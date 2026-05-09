package com.sofka.accounts.controller;

import com.sofka.accounts.dto.AccountRequest;
import com.sofka.accounts.dto.AccountResponse;
import com.sofka.accounts.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{clientId}")
    public Mono<ResponseEntity<List<AccountResponse>>> getAccountsByClientId(
            @PathVariable Long clientId) {
        return accountService.findByClientId(clientId)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{clientId}")
    public Mono<ResponseEntity<AccountResponse>> createAccount(
            @PathVariable Long clientId,
            @Valid @RequestBody AccountRequest request) {
        return accountService.create(clientId, request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
