package com.sofka.accounts.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionReportResponse(LocalDateTime date,
                                        String clientName,
                                        String accountNumber,
                                        String accountType,
                                        BigDecimal initialBalance,
                                        Boolean status,
                                        String movement,
                                        BigDecimal balance) {
}
