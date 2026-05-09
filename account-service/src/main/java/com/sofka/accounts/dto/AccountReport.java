package com.sofka.accounts.dto;

import java.util.List;

import java.math.BigDecimal;

public record AccountReport(
        String accountNumber,
        BigDecimal currentBalance,
        List<TransactionResponse> transactions
) {
}
