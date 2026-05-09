package com.sofka.accounts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "customer.service")
public record CustomerServiceProperties(String url) {
}
