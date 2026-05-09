package com.sofka.accounts.dto;

import com.sofka.accounts.domain.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull TransactionType transactionType,
        @NotNull @Positive BigDecimal amount
) {
}
