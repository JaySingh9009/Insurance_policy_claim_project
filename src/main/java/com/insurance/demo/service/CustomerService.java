package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.CustomerRequest;
import com.insurance.demo.dto.CustomerResponse;

public interface CustomerService {

    CustomerResponse createCustomer(
            CustomerRequest request);

    CustomerResponse getCustomer(
            Long userId);

    CustomerResponse updateCustomer(
            Long customerId,
            CustomerRequest request);
    
    CustomerResponse getMyProfile(String email);

    List<CustomerResponse> getAllCustomers();
}