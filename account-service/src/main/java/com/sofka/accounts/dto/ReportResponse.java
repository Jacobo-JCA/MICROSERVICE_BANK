package com.sofka.accounts.dto;

import java.util.List;

public record ReportResponse(
        String clientName,
        List<AccountReport> accounts
) {
}
