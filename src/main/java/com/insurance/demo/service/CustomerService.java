package com.insurance.demo.service;

import java.util.List;

import com.insurance.demo.dto.CustomerRequest;
import com.insurance.demo.dto.CustomerResponse;

public interface CustomerService {

	CustomerResponse createProfile(String email, CustomerRequest request);

	CustomerResponse getCustomerProfile(String email);

	List<CustomerResponse> getAllCustomers();

	CustomerResponse updateProfile(String email, CustomerRequest request);
}