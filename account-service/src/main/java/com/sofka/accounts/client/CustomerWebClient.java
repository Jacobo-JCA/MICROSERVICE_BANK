package com.sofka.accounts.client;

import com.sofka.accounts.dto.CustomerResponse;
import com.sofka.accounts.exception.BusinessException;
import com.sofka.accounts.exception.ResourceNotFoundException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CustomerWebClient {
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;

    public CustomerWebClient(WebClient.Builder builder,
                             @Value("${customer.service.url}") String baseUrl,
                             CircuitBreakerRegistry registry) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.circuitBreaker = registry.circuitBreaker("customerService");
    }

    public Mono<CustomerResponse> getCustomerById(Long clientId) {
        return webClient.get()
                .uri("/v1/customers/{id}", clientId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new ResourceNotFoundException("Customer not found with id: " + clientId)))
                .bodyToMono(CustomerResponse.class)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(Exception.class, ex ->
                        Mono.error(new BusinessException("Servicio de clientes no disponible")));
    }
}
