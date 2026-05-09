package com.sofka.customers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.customers.dto.CustomerRequest;
import com.sofka.customers.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("Debe crear un cliente correctamente y persistirlo en la base de datos")
    void createCustomer_Success() throws Exception {
        // Arrange
        CustomerRequest request = new CustomerRequest(
                "Juan Perez",
                "Masculino",
                30,
                "1234567890",
                "Calle 123",
                "0987654321",
                "password123"
        );

        // Act
        mockMvc.perform(post("/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Juan Perez"))
                .andExpect(jsonPath("$.identification").value("1234567890"))
                .andExpect(jsonPath("$.state").value(true));

        // Assert: Validar persistencia real
        assertThat(customerRepository.findAll()).hasSize(1);
        assertThat(customerRepository.findAll().get(0).getName()).isEqualTo("Juan Perez");
    }
}
