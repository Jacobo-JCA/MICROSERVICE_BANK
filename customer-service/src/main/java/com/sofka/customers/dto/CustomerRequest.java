package com.sofka.customers.dto;

import jakarta.validation.constraints.*;

public record CustomerRequest(
        @NotBlank(message = "Name cannot be blank")
        String name,

        @NotBlank(message = "Gender cannot be blank")
        String gender,

        @NotNull(message = "Age is required")
        @Min(value = 0, message = "Age must be greater or equal to 0")
        Integer age,

        @NotBlank(message = "Identification is required")
        @Size(max = 10, message = "Identification must be 10 characters")
        String identification,

        @NotBlank(message = "Address cannot be blank")
        String address,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "\\d{10}", message = "Phone number must contain 10 digits")
        String phone,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {
}
