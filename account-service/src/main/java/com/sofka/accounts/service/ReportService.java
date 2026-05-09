package com.sofka.accounts.service;

import com.sofka.accounts.client.CustomerWebClient;
import com.sofka.accounts.domain.Account;
import com.sofka.accounts.domain.Transaction;
import com.sofka.accounts.dto.AccountReport;
import com.sofka.accounts.dto.ReportResponse;
import com.sofka.accounts.dto.TransactionResponse;
import com.sofka.accounts.mapper.TransactionMapper;
import com.sofka.accounts.repository.AccountRepository;
import com.sofka.accounts.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerWebClient customerWebClient;

    public ReportService(AccountRepository accountRepository, TransactionRepository transactionRepository, CustomerWebClient customerWebClient) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.customerWebClient = customerWebClient;
    }

    public Mono<ReportResponse> generateReport(Long clientId, LocalDate startDate, LocalDate endDate) {
        return customerWebClient.getCustomerById(clientId)
                .flatMap(customer -> Mono.fromCallable(() -> buildReport(customer.name(), clientId,
                                startDate.atStartOfDay(),
                                endDate.atTime(23, 59, 59)))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    private ReportResponse buildReport(String clientName, Long clientId, LocalDateTime startDate, LocalDateTime endDate) {
        List<AccountReport> accountReports = accountRepository.findByClientId(clientId)
                .stream()
                .map(account -> buildAccountReport(account, startDate, endDate))
                .collect(Collectors.toList());
        return new ReportResponse(clientName, accountReports);
    }

    private AccountReport buildAccountReport(Account account, LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal currentBalance = transactionRepository.findTopByAccountOrderByDateDesc(account)
                .map(Transaction::getBalance)
                .orElse(account.getInitialBalance());
        List<TransactionResponse> transactions = transactionRepository
                .findByAccountAndDateBetween(account, startDate, endDate)
                .stream()
                .map(transaction -> TransactionMapper.toResponse(transaction,
                        transaction.getBalance().subtract(transaction.getAmount())))
                .collect(Collectors.toList());
        return new AccountReport(account.getAccountNumber(), currentBalance, transactions);
    }
}