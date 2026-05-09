package com.sofka.accounts.dto;

import com.sofka.accounts.domain.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        String clientName,
        String accountNumber,
        AccountType accountType,
        BigDecimal initialBalance,
        Boolean status,
        Long clientId
) {
}

