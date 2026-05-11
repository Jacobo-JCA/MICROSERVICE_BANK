package com.sofka.customers.cucumber.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.customers.dto.CustomerRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.sofka.customers.CustomerServiceApplication;

import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = CustomerServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CustomerSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerRequest customerRequest;
    private ResultActions resultActions;

    @Given("los datos de una persona y una contraseña")
    public void los_datos_de_una_persona_y_una_contrasena() {
        // Arrange
        customerRequest = new CustomerRequest(
                "Juan Perez",
                "M",
                30,
                "1234567890",
                "Calle 123",
                "3001234567",
                "password123"
        );
    }

    @When("se solicita la creacion de un nuevo cliente")
    public void se_solicita_la_creacion_de_un_nuevo_cliente() throws Exception {
        // Act
        resultActions = mockMvc.perform(post("/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerRequest)));
    }

    @Then("el sistema guarda la informacion y asigna un clienteId unico")
    public void el_sistema_guarda_la_informacion_y_asigna_un_clienteId_unico() throws Exception {
        // Assert
        resultActions.andExpect(status().isCreated())
                     .andExpect(jsonPath("$.clientId", notNullValue()))
                     .andExpect(jsonPath("$.name", is("Juan Perez")));
    }

    @Then("el cliente queda en estado activo por defecto")
    public void el_cliente_queda_en_estado_activo_por_defecto() throws Exception {
        // Assert
        resultActions.andExpect(jsonPath("$.state", is(true)));
    }
}
