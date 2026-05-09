package com.sofka.customers.dto;

public record CustomerResponse(
        Long id,
        String clientId,
        String name,
        String gender,
        Integer age,
        String identification,
        String address,
        String phone,
        Boolean state
) {
}
