package com.sofka.accounts.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        LocalDateTime date,
        String transactionType,
        BigDecimal amount,
        BigDecimal balance,
        String accountNumber,
        String movement,
        BigDecimal initialBalance
) {
}
