package com.sofka.customers.service;

import com.sofka.customers.domain.Customer;
import com.sofka.customers.dto.CustomerRequest;
import com.sofka.customers.dto.CustomerResponse;
import com.sofka.customers.exception.ResourceNotFoundException;
import com.sofka.customers.mapper.CustomerMapper;
import com.sofka.customers.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository repository;

    public CustomerServiceImpl(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CustomerResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(CustomerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerResponse findById(Long id) {
        return repository.findById(id)
                .map(CustomerMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private String generateClientId() {
        return "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public CustomerResponse create(CustomerRequest request) {
        Customer customer = CustomerMapper.toEntity(request);
        customer.setState(true);
        customer.setClientId(generateClientId());
        Customer saved = repository.save(customer);
        return CustomerMapper.toResponse(saved);
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        Customer updated = CustomerMapper.toEntity(request);
        updated.setId(customer.getId());
        return CustomerMapper.toResponse(repository.save(updated));
    }

    @Override
    public CustomerResponse partialUpdate(Long id, CustomerRequest request) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        CustomerMapper.patchEntity(customer, request);
        Customer updated = repository.save(customer);
        return CustomerMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customer.setState(false);
        repository.save(customer);
    }
}
