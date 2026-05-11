package com.sofka.accounts.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.accounts.domain.Account;
import com.sofka.accounts.domain.AccountType;
import com.sofka.accounts.domain.TransactionType;
import com.sofka.accounts.dto.TransactionRequest;
import com.sofka.accounts.repository.AccountRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.sofka.accounts.AccountServiceApplication;

import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AccountServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TransactionSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    private Long accountId;
    private ResultActions resultActions;

    @Given("una cuenta con saldo {int}")
    public void una_cuenta_con_saldo(Integer initialBalance) {
        // Arrange
        accountRepository.deleteAll(); // Limpiar entorno para asegurar tests idempotentes
        Account account = new Account();
        account.setClientId(1L);
        account.setInitialBalance(BigDecimal.valueOf(initialBalance));
        account.setTypeAccount(AccountType.SAVINGS);
        account.setStatus(true);
        
        Account savedAccount = accountRepository.save(account);
        this.accountId = savedAccount.getIdAccount();
    }

    @When("se intenta realizar un retiro por un valor mayor a {int}")
    public void se_intenta_realizar_un_retiro_por_un_valor_mayor_a(Integer valor) throws Exception {
        // Act
        TransactionRequest request = new TransactionRequest(
                TransactionType.WITHDRAWAL,
                BigDecimal.valueOf(valor + 500) // Se excede el saldo inicial provisto
        );

        resultActions = mockMvc.perform(post("/v1/transactions/" + accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @Then("el sistema debe rechazar la operacion")
    public void el_sistema_debe_rechazar_la_operacion() throws Exception {
        // Assert
        resultActions.andExpect(status().isConflict()); // Validamos el HttpStatus mapeado para InsufficientBalanceException
    }

    @Then("mostrar el mensaje {string}")
    public void mostrar_el_mensaje(String mensaje) throws Exception {
        // Assert
        resultActions.andExpect(jsonPath("$.message").value(mensaje));
    }
}
