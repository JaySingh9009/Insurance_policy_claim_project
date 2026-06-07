package com.insurance.demo.service;

import com.insurance.demo.dto.CustomerRequest;
import com.insurance.demo.dto.CustomerResponse;

public interface CustomerService {

    CustomerResponse createCustomer(
            CustomerRequest request);

    CustomerResponse getCustomer(
            Long userId);
}