package com.sofka.accounts.service;

import com.sofka.accounts.client.CustomerWebClient;
import com.sofka.accounts.domain.Account;
import com.sofka.accounts.domain.Transaction;
import com.sofka.accounts.domain.TransactionType;
import com.sofka.accounts.dto.TransactionReportResponse;
import com.sofka.accounts.dto.TransactionRequest;
import com.sofka.accounts.dto.TransactionResponse;
import com.sofka.accounts.exception.BusinessException;
import com.sofka.accounts.exception.InsufficientBalanceException;
import com.sofka.accounts.exception.ResourceNotFoundException;
import com.sofka.accounts.mapper.TransactionMapper;
import com.sofka.accounts.repository.AccountRepository;
import com.sofka.accounts.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CustomerWebClient customerWebClient;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository,
                                  CustomerWebClient customerWebClient) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.customerWebClient = customerWebClient;
    }

    @Override
    public List<TransactionResponse> findAll() {
        return transactionRepository.findAll()
                .stream()
                .map(transaction -> TransactionMapper.toResponse(transaction,
                        transaction.getBalance().subtract(transaction.getAmount())))
                .collect(Collectors.toList());
    }

    @Override
    public Mono<List<TransactionReportResponse>> findByClientAndDates(Long clientId, LocalDate startDate, LocalDate endDate) {
        return customerWebClient.getCustomerById(clientId)
                .flatMap(customer -> Mono.fromCallable(() ->
                        transactionRepository.findByAccountClientIdAndDateBetween(
                                        clientId,
                                        startDate.atStartOfDay(),
                                        endDate.atTime(23, 59, 59)
                                )
                                .stream()
                                .map(transaction -> TransactionMapper.toReportResponse(transaction, customer.name()))
                                .collect(Collectors.toList())
                ).subscribeOn(Schedulers.boundedElastic()));
    }

    private Account findAndValidateAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        if (!account.getStatus()) {
            throw new BusinessException("Account is inactive");
        }
        return account;
    }

    private BigDecimal getCurrentBalance(Account account) {
        return transactionRepository.findTopByAccountOrderByDateDesc(account)
                .map(Transaction::getBalance)
                .orElse(account.getInitialBalance());
    }

    private BigDecimal calculateNewBalance(BigDecimal currentBalance, TransactionRequest request) {
        BigDecimal newBalance = request.transactionType() == TransactionType.DEPOSIT
                ? currentBalance.add(request.amount())
                : currentBalance.subtract(request.amount());

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException("Saldo no disponible");
        }
        return newBalance;
    }

    @Override
    public TransactionResponse create(Long accountId, TransactionRequest request) {
        Account account = findAndValidateAccount(accountId);
        BigDecimal currentBalance = getCurrentBalance(account);
        BigDecimal newBalance = calculateNewBalance(currentBalance, request);
        Transaction saved = transactionRepository.save(TransactionMapper.toEntity(request, account, newBalance));
        return TransactionMapper.toResponse(saved, currentBalance);
    }
}