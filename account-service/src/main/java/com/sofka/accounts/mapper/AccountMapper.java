package com.sofka.accounts.mapper;

import com.sofka.accounts.domain.Account;
import com.sofka.accounts.domain.Transaction;
import com.sofka.accounts.dto.AccountRequest;
import com.sofka.accounts.dto.AccountResponse;
import com.sofka.accounts.dto.TransactionResponse;

import java.util.Optional;

public class AccountMapper {

    private AccountMapper() {
    }

    public static AccountResponse toResponse(Account account, String clientName) {
        return new AccountResponse(
                clientName,
                account.getAccountNumber(),
                account.getTypeAccount(),
                account.getInitialBalance(),
                account.getStatus(),
                account.getClientId()
        );
    }


    public static Account toEntity(AccountRequest request, Long clientId) {
        Account account = new Account();
        account.setTypeAccount(request.accountType());
        account.setInitialBalance(request.initialBalance());
        account.setClientId(clientId);
        return account;
    }
}
