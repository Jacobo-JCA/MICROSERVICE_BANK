package com.sofka.accounts.dto;

import com.sofka.accounts.domain.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AccountRequest(
        @NotNull AccountType accountType,
        @NotNull @PositiveOrZero BigDecimal initialBalance
) {
}
