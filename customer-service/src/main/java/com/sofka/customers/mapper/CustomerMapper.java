package com.sofka.customers.mapper;

import com.sofka.customers.domain.Customer;
import com.sofka.customers.dto.CustomerRequest;
import com.sofka.customers.dto.CustomerResponse;

import java.util.Optional;
import java.util.UUID;

public class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getClientId(),
                customer.getName(),
                customer.getGender(),
                customer.getAge(),
                customer.getIdentification(),
                customer.getAddress(),
                customer.getPhone(),
                customer.getState()
        );
    }

    public static Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setGender(request.gender());
        customer.setAge(request.age());
        customer.setIdentification(request.identification());
        customer.setAddress(request.address());
        customer.setPhone(request.phone());
        customer.setPassword(request.password());
        return customer;
    }

    public static void patchEntity(Customer customer, CustomerRequest request) {
        Optional.ofNullable(request.name()).ifPresent(customer::setName);
        Optional.ofNullable(request.gender()).ifPresent(customer::setGender);
        Optional.ofNullable(request.age()).ifPresent(customer::setAge);
        Optional.ofNullable(request.identification()).ifPresent(customer::setIdentification);
        Optional.ofNullable(request.address()).ifPresent(customer::setAddress);
        Optional.ofNullable(request.phone()).ifPresent(customer::setPhone);
        Optional.ofNullable(request.password()).ifPresent(customer::setPassword);
    }

}
