package com.sofka.accounts.service;

import com.sofka.accounts.client.CustomerWebClient;
import com.sofka.accounts.domain.Account;
import com.sofka.accounts.dto.AccountRequest;
import com.sofka.accounts.dto.AccountResponse;
import com.sofka.accounts.exception.ResourceNotFoundException;
import com.sofka.accounts.mapper.AccountMapper;
import com.sofka.accounts.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {
    private final AccountRepository repository;
    private final CustomerWebClient customerWebClient;

    public AccountServiceImpl(AccountRepository repository, CustomerWebClient customerWebClient) {
        this.repository = repository;
        this.customerWebClient = customerWebClient;
    }

    @Override
    public Mono<List<AccountResponse>> findByClientId(Long clientId) {
        return customerWebClient.getCustomerById(clientId)
                .flatMap(customer -> Mono.fromCallable(() ->
                        repository.findByClientId(clientId)
                                .stream()
                                .map(account -> AccountMapper.toResponse(account, customer.name()))
                                .toList()
                ).subscribeOn(Schedulers.boundedElastic()));
    }

    @Override
    public Mono<AccountResponse> create(Long clientId, AccountRequest request) {
        return customerWebClient.getCustomerById(clientId)
                .flatMap(customer -> {
                    return Mono.fromCallable(() -> {
                        Account account = AccountMapper.toEntity(request, clientId);
                        Account saved = repository.save(account);
                        return AccountMapper.toResponse(saved, customer.name());
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }

    @Override
    public void delete(Long id) {
        Account account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        account.setStatus(false);
        repository.save(account);
    }
}
