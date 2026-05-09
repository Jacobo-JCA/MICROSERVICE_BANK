package com.sofka.customers.service;

import com.sofka.customers.domain.Customer;
import com.sofka.customers.dto.CustomerRequest;
import com.sofka.customers.dto.CustomerResponse;
import com.sofka.customers.exception.ResourceNotFoundException;
import com.sofka.customers.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void shouldCreateCustomerWithGeneratedClientIdWhenRequestIsValid() {
        CustomerRequest request = new CustomerRequest(
                "José Pérez",
                "Masculino",
                35,
                "1234567890",
                "Calle 123, Bogotá",
                "3001234567",
                "securePass1"
        );

        Customer savedCustomer = buildCustomer(1L, "CUST-A1B2C3D4", "José Pérez", true);

        when(repository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerResponse response = customerService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.clientId()).startsWith("CUST-");
        assertThat(response.clientId()).hasSize(13); // "CUST-" (5) + 8 chars
        assertThat(response.name()).isEqualTo("José Pérez");
        assertThat(response.state()).isTrue();

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repository).save(captor.capture());

        Customer persisted = captor.getValue();
        assertThat(persisted.getState()).isTrue();
        assertThat(persisted.getClientId()).matches("CUST-[A-F0-9]{8}");

        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenCustomerDoesNotExist() {
        Long nonExistentId = 999L;
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with id: " + nonExistentId);

        verify(repository).findById(nonExistentId);
        verifyNoMoreInteractions(repository);
    }

    private Customer buildCustomer(Long id, String clientId, String name, Boolean state) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setClientId(clientId);
        customer.setName(name);
        customer.setGender("Masculino");
        customer.setAge(35);
        customer.setIdentification("1234567890");
        customer.setAddress("Calle 123, Bogotá");
        customer.setPhone("3001234567");
        customer.setPassword("securePass1");
        customer.setState(state);
        return customer;
    }
}
