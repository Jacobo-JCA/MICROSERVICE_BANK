package com.sofka.accounts.service;

import com.sofka.accounts.domain.Account;
import com.sofka.accounts.domain.AccountType;
import com.sofka.accounts.domain.Transaction;
import com.sofka.accounts.domain.TransactionType;
import com.sofka.accounts.dto.TransactionRequest;
import com.sofka.accounts.dto.TransactionResponse;
import com.sofka.accounts.exception.InsufficientBalanceException;
import com.sofka.accounts.repository.AccountRepository;
import com.sofka.accounts.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void shouldCreateDepositTransactionWhenAccountIsActive() {
        Long accountId = 1L;
        Account account = createMockAccount(accountId, new BigDecimal("1000.00"));
        TransactionRequest request = new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("500.00"));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.findTopByAccountOrderByDateDesc(account)).thenReturn(Optional.empty());
        
        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(10L);
        savedTransaction.setAmount(new BigDecimal("500.00"));
        savedTransaction.setBalance(new BigDecimal("1500.00"));
        savedTransaction.setAccount(account);
        savedTransaction.setTransactionType(TransactionType.DEPOSIT);
        
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponse response = transactionService.create(accountId, request);

        assertThat(response).isNotNull();
        assertThat(response.balance()).isEqualByComparingTo("1500.00");
        assertThat(response.movement()).isEqualTo("+500.00");
        
        verify(accountRepository).findById(accountId);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldThrowInsufficientBalanceExceptionWhenAmountExceedsBalance() {
        Long accountId = 1L;
        Account account = createMockAccount(accountId, new BigDecimal("100.00"));
        TransactionRequest request = new TransactionRequest(TransactionType.WITHDRAWAL, new BigDecimal("500.00"));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.findTopByAccountOrderByDateDesc(account)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(accountId, request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Saldo no disponible");

        verify(accountRepository).findById(accountId);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    private Account createMockAccount(Long id, BigDecimal initialBalance) {
        Account account = new Account();
        account.setIdAccount(id);
        account.setAccountNumber("123456");
        account.setInitialBalance(initialBalance);
        account.setStatus(true);
        account.setTypeAccount(AccountType.SAVINGS);
        return account;
    }

}
