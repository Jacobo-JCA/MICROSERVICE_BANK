package com.sofka.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.accounts.client.CustomerWebClient;
import com.sofka.accounts.domain.Account;
import com.sofka.accounts.domain.AccountType;
import com.sofka.accounts.domain.Transaction;
import com.sofka.accounts.domain.TransactionType;
import com.sofka.accounts.dto.TransactionRequest;
import com.sofka.accounts.repository.AccountRepository;
import com.sofka.accounts.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private CustomerWebClient customerWebClient;

    private Account savedAccount;

    @BeforeEach
    void setUp() {
        // Limpiar para asegurar independencia
        transactionRepository.deleteAll();
        accountRepository.deleteAll();

        // Arrange: Preparar datos iniciales (Cuenta con saldo)
        Account account = new Account();
        account.setInitialBalance(new BigDecimal("1000.00"));
        account.setTypeAccount(AccountType.SAVINGS);
        account.setStatus(true);
        account.setClientId(1L);
        // El número de cuenta se genera en @PrePersist
        
        savedAccount = accountRepository.save(account);
    }

    @Test
    @DisplayName("Debe crear un depósito exitosamente y actualizar el saldo acumulado")
    void createDeposit_Success() throws Exception {
        // Arrange
        Long accountId = savedAccount.getIdAccount();
        BigDecimal depositAmount = new BigDecimal("500.00");
        TransactionRequest request = new TransactionRequest(TransactionType.DEPOSIT, depositAmount);

        // Act: Ejecutar request HTTP real al endpoint del controlador
        mockMvc.perform(post("/v1/transactions/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(500.0))
                .andExpect(jsonPath("$.balance").value(1500.0)) // 1000 + 500
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"));

        // Assert: Validar persistencia real en base de datos (Repository)
        List<Transaction> transactions = transactionRepository.findAll();
        assertThat(transactions).hasSize(1);
        
        Transaction savedTransaction = transactions.get(0);
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo(depositAmount);
        assertThat(savedTransaction.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(savedTransaction.getAccount().getIdAccount()).isEqualTo(accountId);
    }

    @Test
    @DisplayName("Debe fallar al realizar un retiro mayor al saldo disponible")
    void createWithdrawal_InsufficientBalance() throws Exception {
        // Arrange
        Long accountId = savedAccount.getIdAccount();
        BigDecimal withdrawalAmount = new BigDecimal("1500.00"); // Mayor a los 1000 iniciales
        TransactionRequest request = new TransactionRequest(TransactionType.WITHDRAWAL, withdrawalAmount);

        // Act & Assert
        mockMvc.perform(post("/v1/transactions/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Saldo no disponible"));

        // Validar que no se persistió nada
        assertThat(transactionRepository.findAll()).isEmpty();
    }
}
