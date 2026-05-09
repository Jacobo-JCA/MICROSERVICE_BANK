package com.sofka.accounts.mapper;

import com.sofka.accounts.domain.Account;
import com.sofka.accounts.domain.Transaction;
import com.sofka.accounts.domain.TransactionType;
import com.sofka.accounts.dto.TransactionReportResponse;
import com.sofka.accounts.dto.TransactionRequest;
import com.sofka.accounts.dto.TransactionResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionMapper {

    public static Transaction toEntity(TransactionRequest request, Account account, BigDecimal newBalance) {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDateTime.now());
        transaction.setTransactionType(request.transactionType());
        transaction.setAmount(request.amount());
        transaction.setBalance(newBalance);
        transaction.setAccount(account);
        return transaction;
    }

    public static TransactionReportResponse toReportResponse(Transaction transaction, String clientName) {
        BigDecimal initialBalance = transaction.getBalance().subtract(transaction.getAmount());
        String movement = transaction.getTransactionType() == TransactionType.DEPOSIT
                ? "+" + transaction.getAmount()
                : "-" + transaction.getAmount();
        return new TransactionReportResponse(
                transaction.getDate(),
                clientName,
                transaction.getAccount().getAccountNumber(),
                transaction.getAccount().getTypeAccount().name(),
                initialBalance,
                transaction.getAccount().getStatus(),
                movement,
                transaction.getBalance()
        );
    }

    public static TransactionResponse toResponse(Transaction transaction, BigDecimal initialBalance) {
        String movement = transaction.getTransactionType() == TransactionType.DEPOSIT
                ? "+" + transaction.getAmount()
                : "-" + transaction.getAmount();
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDate(),
                transaction.getTransactionType().name(),
                transaction.getAmount(),
                transaction.getBalance(),
                transaction.getAccount().getAccountNumber(),
                movement,
                initialBalance
        );
    }


}
