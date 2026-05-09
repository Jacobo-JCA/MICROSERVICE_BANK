package com.sofka.accounts.service;

import com.sofka.accounts.dto.AccountRequest;
import com.sofka.accounts.dto.AccountResponse;
import reactor.core.publisher.Mono;

import java.util.List;


public interface AccountService {
    Mono<List<AccountResponse>> findByClientId(Long clientId);
    Mono<AccountResponse> create(Long clientId, AccountRequest request);
    void delete(Long id);
}
