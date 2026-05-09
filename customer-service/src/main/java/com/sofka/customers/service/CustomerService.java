package com.sofka.customers.service;

import com.sofka.customers.dto.CustomerRequest;
import com.sofka.customers.dto.CustomerResponse;

import java.util.List;

public interface CustomerService {
    List<CustomerResponse> findAll();
    CustomerResponse findById(Long id);
    CustomerResponse create(CustomerRequest request);
    CustomerResponse update(Long id, CustomerRequest request);
    CustomerResponse partialUpdate(Long id, CustomerRequest request);
    void delete(Long id);
}
